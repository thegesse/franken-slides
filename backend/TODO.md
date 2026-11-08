# Service layer TODO

Scope: backend service classes only. The frontend assembles selected slide groups into a temporary presentation for playback; do not persist a `Presentation` entity as part of this work.

## ThemeService

- [x] Implement creating a theme for a user.
- [x] Implement retrieving theme details by ID. Groups and slides can be fetched through their own service operations when needed.
- [x] Implement updating a theme's name.
- [x] Implement deleting a theme.
- [x] Check that the requesting user owns the theme before returning or changing it.
- [x] Keep theme response mapping in the service layer and avoid exposing JPA entities directly.
- [x] Keep the paginated `findByOwnerId` query; remove the unused unpaged single-result method.

## SlideGroup service

- [x] Implement creating, listing, retrieving, renaming, moving between themes, and deleting slide groups.
- [X] Register `SlideGroupService` as a Spring bean with `@Service`.
- [x] Verify that the parent theme exists and belongs to the requesting user before creating or modifying a group.
- [X] Return groups in a stable order suitable for the frontend to display and select.
- [x] Map groups to DTOs rather than returning entities with lazy-loaded relationships.

## Slide service

- [x] Add a service for creating, listing, retrieving, updating, and deleting slides.
- [x] Verify that the parent group exists and its theme belongs to the requesting user before any operation.
- [x] Return slides in stable creation order (ID ascending) for playback.
- [x] Default blank title and description values to `empty`; replace missing or empty-object content with `{ "text": "empty" }` and reject non-object content.
- [x] Map slides to DTOs rather than returning JPA entities.

## UserService

- [x] Restrict user lookup and deletion operations to self-service or admin-only use cases.
- [x] Remove the unused `Pageable` parameter from the email lookup.
- [x] Use clear exceptions for missing users and unauthorized or invalid operations.

## AuthService

- [x] Expose registration and login through public service methods so application entry points can call them.
- [X] Return a clear authentication error when a username is not found or a password does not match.
- [X] Reject duplicate email addresses with a specific exception rather than a generic `RuntimeException`.
- [x] Keep password hashing in the service and never include passwords or password hashes in response DTOs.

## Shared service concerns

- [ ] Use transactions for service operations that read or update related entities.
- [x] Keep authorization and ownership checks in service operations so every caller gets the same protection.
- [x] Define response DTOs so services do not return JPA entities directly.
- [ ] Define request DTOs and consistent validation rules for service inputs.
- [ ] Remove or update TODO comments in services as the corresponding work is completed.
