# Contributing

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/) for every commit. Each commit should have a concise subject and a short body explaining what changed and why:

```text
<type>(<scope>): <imperative summary>

<short explanation of the change and its reason>
```

Use these types:

- `feat`: add user-visible functionality
- `fix`: correct broken behavior
- `docs`: documentation-only changes
- `test`: add or change tests
- `refactor`: change code structure without changing behavior
- `build`: change dependencies or build configuration
- `ci`: change automation or pipeline configuration
- `chore`: maintenance that does not affect application behavior

Keep the summary short, specific, and written in the imperative mood. Examples:

```text
feat(backend): add product creation endpoint

Expose the product creation workflow through the backend API so authenticated users can add inventory items.

fix(database): align identifier columns with JPA entities

Widen primary-key and foreign-key columns to BIGINT so Flyway and Hibernate validate the same schema contract.

fix(security): bypass JWT validation for public health endpoints

Allow health and documentation requests to remain public when clients send stale or malformed authorization headers.
```

## Safe commit and push workflow

Run the checks before committing:

```bash
cd backend && mvn test
cd ../frontend && npm test -- --run
cd .. && docker compose up -d --build
curl -fsS http://localhost:8080/actuator/health
```

Review exactly what will be committed, then create one focused commit:

```bash
git status
git diff --check
git add <files-for-one-change>
git diff --cached --check
git diff --cached --stat
git commit -m "type(scope): imperative summary"
```

Push the branch after the commit succeeds:

```bash
git push -u origin main
```

Never commit `.env`, passwords, JWT secrets, database backups, build output, or dependency directories. Keep `.env.example` files updated with safe placeholder values instead.