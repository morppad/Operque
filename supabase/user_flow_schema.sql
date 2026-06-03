create table if not exists public.tasks (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles(id) on delete cascade,
    title text not null,
    description text,
    status text not null default 'todo',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.comments (
    id uuid primary key default gen_random_uuid(),
    task_id uuid not null references public.tasks(id) on delete cascade,
    user_id uuid not null references public.profiles(id) on delete cascade,
    text text not null,
    created_at timestamptz not null default now()
);

alter table public.tasks enable row level security;
alter table public.comments enable row level security;

drop policy if exists "Users can read own tasks" on public.tasks;
create policy "Users can read own tasks"
on public.tasks
for select
using (auth.uid() = user_id);

drop policy if exists "Users can create own tasks" on public.tasks;

drop policy if exists "Users can update own tasks" on public.tasks;
create policy "Users can update own tasks"
on public.tasks
for update
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists "Users can read comments on own tasks" on public.comments;
create policy "Users can read comments on own tasks"
on public.comments
for select
using (
    exists (
        select 1
        from public.tasks
        where tasks.id = comments.task_id
          and tasks.user_id = auth.uid()
    )
);

drop policy if exists "Users can comment on own tasks" on public.comments;
create policy "Users can comment on own tasks"
on public.comments
for insert
with check (
    auth.uid() = user_id
    and exists (
        select 1
        from public.tasks
        where tasks.id = comments.task_id
          and tasks.user_id = auth.uid()
    )
);
