# Service layer TODO

Scope: backend service classes only. The frontend assembles selected slide groups into a temporary presentation for playback; do not persist a `Presentation` entity as part of this work.

## ThemeService

- [ ] Implement creating a theme for a user.
- [ ] Implement retrieving a theme by ID, including its groups and slides as needed by the caller.
- [ ] Implement updating a theme's name.
- [ ] Implement deleting a theme.
- [ ] Check that the requesting user owns the theme before returning or changing it.
- [ ] Keep theme response mapping in the service layer and avoid exposing JPA entities directly.
- [ ] Review the existing `findByOwnerId` repository methods: keep the paginated query and remove or use the unpaged single-result method appropriately.

## SlideGroup service

- [ ] Add a service for creating, listing, retrieving, renaming, and deleting slide groups.
- [ ] Verify that the parent theme exists and belongs to the requesting user before creating or modifying a group.
- [ ] Return groups in a stable order suitable for the frontend to display and select.
- [ ] Map groups to DTOs rather than returning entities with lazy-loaded relationships.

## Slide service

- [ ] Add a service for creating, listing, retrieving, updating, and deleting slides.
- [ ] Verify that the parent group exists and its theme belongs to the requesting user before any operation.
- [ ] Return slides in their intended playback order.
- [ ] Validate required slide fields and reject invalid or empty content.
- [ ] Map slides to DTOs rather than returning JPA entities.

## UserService

- [ ] Restrict user lookup and deletion operations to authorized service use cases.
- [ ] Review `findUserByEmail`: its `Pageable` parameter is unused, so remove it or change the method to return a page if that is the intended behavior.
- [ ] Use clear, consistent exceptions for missing users and invalid operations.

## AuthService

- [ ] Expose registration and login through public service methods so application entry points can call them.
- [ ] Return a clear authentication error when a username is not found or a password does not match.
- [ ] Reject duplicate email addresses with a specific exception rather than a generic `RuntimeException`.
- [ ] Keep password hashing in the service and never include passwords or password hashes in response DTOs.

## Shared service concerns

- [ ] Use transactions for service operations that read or update related entities.
- [ ] Keep authorization and ownership checks in service operations so every caller gets the same protection.
- [ ] Define service-facing DTOs and validation rules for inputs and outputs.
- [ ] Remove or update TODO comments in services as the corresponding work is completed.
