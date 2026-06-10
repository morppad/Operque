create or replace function public.current_profile_role()
returns text
language sql
security definer
set search_path = public
stable
as $$
    select role
    from public.profiles
    where id = auth.uid()
$$;

create or replace function public.can_manage_tasks()
returns boolean
language sql
security definer
set search_path = public
stable
as $$
    select coalesce(public.current_profile_role() in ('manager', 'admin'), false)
$$;

drop policy if exists "Managers can read profiles" on public.profiles;
create policy "Managers can read profiles"
on public.profiles
for select
using (auth.uid() = id or public.can_manage_tasks());

drop policy if exists "Managers can update user roles" on public.profiles;
drop policy if exists "Profiles are updatable by owner" on public.profiles;

create or replace function public.update_user_role(target_user_id uuid, new_role text)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    caller_role text := public.current_profile_role();
    target_role text;
begin
    if caller_role is null or caller_role not in ('manager', 'admin') then
        raise exception 'Only managers can update roles';
    end if;

    if auth.uid() = target_user_id then
        raise exception 'You cannot update your own role';
    end if;

    if new_role not in ('user', 'manager', 'admin') then
        raise exception 'Unknown role';
    end if;

    select role into target_role
    from public.profiles
    where id = target_user_id;

    if target_role is null then
        raise exception 'Profile not found';
    end if;

    if caller_role <> 'admin' and (target_role = 'admin' or new_role = 'admin') then
        raise exception 'Only administrators can modify administrators';
    end if;

    update public.profiles
    set role = new_role
    where id = target_user_id;
end;
$$;

revoke all on function public.update_user_role(uuid, text) from public;
grant execute on function public.update_user_role(uuid, text) to authenticated;

drop policy if exists "Managers can read tasks" on public.tasks;
create policy "Managers can read tasks"
on public.tasks
for select
using (public.can_manage_tasks());

drop policy if exists "Managers can create tasks" on public.tasks;
create policy "Managers can create tasks"
on public.tasks
for insert
with check (
    public.can_manage_tasks()
    and exists (
        select 1
        from public.profiles
        where profiles.id = tasks.user_id
          and profiles.role = 'user'
    )
);

drop policy if exists "Managers can update tasks" on public.tasks;
create policy "Managers can update tasks"
on public.tasks
for update
using (public.can_manage_tasks())
with check (public.can_manage_tasks());

drop policy if exists "Managers can delete tasks" on public.tasks;
create policy "Managers can delete tasks"
on public.tasks
for delete
using (public.can_manage_tasks());

drop policy if exists "Managers can read comments" on public.comments;
create policy "Managers can read comments"
on public.comments
for select
using (public.can_manage_tasks());

drop policy if exists "Managers can create comments" on public.comments;
create policy "Managers can create comments"
on public.comments
for insert
with check (auth.uid() = user_id and public.can_manage_tasks());
