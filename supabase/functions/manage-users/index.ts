import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authorization = request.headers.get("Authorization");
    if (!authorization) return json({ error: "Unauthorized" }, 401);

    const url = Deno.env.get("SUPABASE_URL")!;
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

    const callerClient = createClient(url, anonKey, {
      global: { headers: { Authorization: authorization } },
    });
    const adminClient = createClient(url, serviceRoleKey);

    const { data: { user: caller } } = await callerClient.auth.getUser();
    if (!caller) return json({ error: "Unauthorized" }, 401);

    const { data: callerProfile } = await adminClient
      .from("profiles")
      .select("role")
      .eq("id", caller.id)
      .single();

    const callerRole = callerProfile?.role;
    if (callerRole !== "manager" && callerRole !== "admin") {
      return json({ error: "Forbidden" }, 403);
    }

    const body = await request.json();
    const action = body.action as string;
    const targetRole = body.role as string | undefined;
    const allowedRoles = callerRole === "admin" ? ["user", "manager", "admin"] : ["user", "manager"];

    if (targetRole && !allowedRoles.includes(targetRole)) {
      return json({ error: "Role is not allowed" }, 403);
    }

    if (action === "create") {
      if (!body.email || !body.password || !targetRole) {
        return json({ error: "Email, password and role are required" }, 400);
      }

      const { data, error } = await adminClient.auth.admin.createUser({
        email: body.email,
        password: body.password,
        email_confirm: true,
      });
      if (error) throw error;

      const { error: profileError } = await adminClient
        .from("profiles")
        .upsert({ id: data.user.id, email: body.email, role: targetRole });
      if (profileError) throw profileError;

      return json({ ok: true });
    }

    if (!body.userId) return json({ error: "userId is required" }, 400);
    if (body.userId === caller.id) return json({ error: "You cannot modify your own account" }, 400);

    const { data: targetProfile } = await adminClient
      .from("profiles")
      .select("role")
      .eq("id", body.userId)
      .single();

    if (callerRole !== "admin" && targetProfile?.role === "admin") {
      return json({ error: "Only administrators can modify administrators" }, 403);
    }

    if (action === "update_role") {
      if (!targetRole) return json({ error: "role is required" }, 400);
      const { error } = await adminClient.from("profiles").update({ role: targetRole }).eq("id", body.userId);
      if (error) throw error;
      return json({ ok: true });
    }

    if (action === "delete") {
      const { error } = await adminClient.auth.admin.deleteUser(body.userId);
      if (error) throw error;
      return json({ ok: true });
    }

    return json({ error: "Unknown action" }, 400);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    return json({ error: message || "Request failed" }, 400);
  }
});

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
