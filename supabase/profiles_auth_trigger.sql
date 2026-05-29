create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    email text not null,
    role text not null default 'user',
    created_at timestamptz not null default now()
);

create or replace function public.create_profile_for_auth_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, email, role)
    values (new.id, new.email, 'user')
    on conflict (id) do nothing;

    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;

create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.create_profile_for_auth_user();

alter table public.profiles enable row level security;

drop policy if exists "Profiles are visible to owner" on public.profiles;
create policy "Profiles are visible to owner"
on public.profiles
for select
using (auth.uid() = id);

drop policy if exists "Profiles are insertable by owner" on public.profiles;
create policy "Profiles are insertable by owner"
on public.profiles
for insert
with check (auth.uid() = id);

drop policy if exists "Profiles are updatable by owner" on public.profiles;
create policy "Profiles are updatable by owner"
on public.profiles
for update
using (auth.uid() = id)
with check (auth.uid() = id);

insert into public.profiles (id, email, role)
select id, email, 'user'
from auth.users
where email is not null
on conflict (id) do nothing;
