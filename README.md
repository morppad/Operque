# Operque

Operque — Android-приложение для управления задачами сотрудников с разделением ролей и защищённым доступом к данным через Supabase.

## Возможности

### Сотрудник

- просмотр назначенных задач;
- изменение статуса собственной задачи;
- просмотр и добавление комментариев.

### Менеджер и администратор

- просмотр задач команды;
- создание задач с выбором исполнителя;
- изменение статуса и удаление задач;
- просмотр и создание пользователей;
- изменение ролей и удаление пользователей.

## Технологии

- Kotlin;
- Jetpack Compose;
- MVVM;
- Navigation Compose;
- Supabase Auth;
- Supabase PostgREST;
- PostgreSQL Row Level Security;
- Supabase Edge Functions.

## Архитектура

```text
Compose UI -> ViewModel -> Repository -> Supabase
```

- `data/model` — модели данных и DTO;
- `data/repository` — авторизация и запросы к Supabase;
- `data/services` — настройка Supabase-клиента и навигация;
- `ui/screens` — Compose-экраны и ViewModel;
- `ui/theme` — тема приложения;
- `supabase` — SQL-схемы, RLS-политики и Edge Function.

## База данных

Supabase Auth хранит учётные записи в `auth.users`. Таблица `profiles` содержит только дополнительные данные пользователя.

```text
profiles
- id          uuid, PRIMARY KEY, FOREIGN KEY -> auth.users.id
- email       text
- role        text, default: user
- created_at  timestamptz

tasks
- id
- user_id     FOREIGN KEY -> profiles.id
- title
- description
- status
- created_at
- updated_at

comments
- id
- task_id     FOREIGN KEY -> tasks.id
- user_id     FOREIGN KEY -> profiles.id
- text
- created_at
```

После регистрации триггер автоматически создаёт запись в `profiles`.

## Безопасность

Для таблиц включён Row Level Security:

- сотрудник видит и изменяет только собственные задачи;
- сотрудник видит комментарии только к своим задачам;
- создавать и удалять задачи могут менеджеры и администраторы;
- пользователь не может самостоятельно изменить собственную роль.

Смена роли выполняется защищённой PostgreSQL RPC-функцией `update_user_role`.

Edge Function `manage-users` используется для безопасного создания и удаления пользователей через Supabase Auth. Секретный `service_role` ключ остаётся на стороне Supabase и не попадает в Android-приложение.

## Настройка Supabase

1. Создайте проект в Supabase.
2. В SQL Editor последовательно выполните:

```text
supabase/profiles_auth_trigger.sql
supabase/user_flow_schema.sql
supabase/manager_flow_schema.sql
```

3. Установите Supabase CLI и авторизуйтесь:

```bash
supabase login
```

4. Свяжите локальный проект с Supabase:

```bash
supabase link --project-ref YOUR_PROJECT_REFERENCE
```

5. Разверните Edge Function:

```bash
supabase functions deploy manage-users
```

## Настройка Android-приложения

Добавьте параметры проекта Supabase в `local.properties`:

```properties
SUPABASE_URL=https://YOUR_PROJECT_REFERENCE.supabase.co
SUPABASE_ANON_KEY=YOUR_ANON_KEY
```

Не добавляйте `service_role` ключ в приложение или `local.properties`.

## Запуск

Откройте проект в Android Studio и запустите конфигурацию `app` на эмуляторе или устройстве с Android 7.0 и выше.

Для проверки сборки:

```bash
./gradlew :app:compileDebugKotlin
```

В Windows:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

## Роли

- `user` — сотрудник;
- `manager` — менеджер;
- `admin` — администратор.

Новый пользователь получает роль `user` по умолчанию. Первого менеджера или администратора необходимо назначить вручную через Supabase Dashboard.
