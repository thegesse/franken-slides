# Backend TODO

Scope: backend service and controller work. The frontend assembles selected slide groups into a temporary presentation for playback; do not persist a `Presentation` entity as part of this work.

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

- [X] Add `@Transactional` only to service operations that need multiple database changes to succeed or fail atomically.
- [x] Keep authorization and ownership checks in service operations so every caller gets the same protection.
- [x] Define response DTOs so services do not return JPA entities directly.
- [ ] Define consistent validation rules for service inputs.
- [X] Remove or update TODO comments in services as the corresponding work is completed.

## Controller layer

The backend currently has no REST controllers. Controllers should delegate business rules to the existing services and return response DTOs. Cognito is planned to handle sign-in and sign-up, so backend login/register endpoints are not included here.

### Request DTOs and identity

- [x] Create request DTOs for theme, slide-group, and slide create/update operations.
- [ ] Validate requests consistently (for example, use `@Valid` and field constraints); validate the agreed structured slide-content shape.
- [X] Return the existing response DTOs; never expose JPA entities or password fields.
- [ ] Obtain the acting user's ID and role from the authenticated Cognito identity or trusted API Gateway context. Never accept `ownerId` or `actorId` from request bodies or query parameters.

### Theme API

- [X] Add endpoints to list the current user's themes, create a theme, get one by ID, rename it, and delete it.
- [X] Return `201 Created` for creation and `204 No Content` for successful deletion; use appropriate statuses for other operations.

### Slide-group API

- [ ] Add endpoints to list groups in a theme, create a group, get one by ID, rename it, move it to another owned theme, and delete it.
- [ ] For group moves, accept a target theme ID and delegate ownership checks to `SlideGroupService`.

### Slide API

- [ ] Add endpoints to list a group's slides in stable order, create a slide, get one by ID, search by title, update slide fields/content, and delete a slide.
- [ ] Accept structured slide content as a JSON object and return it as a JSON object.

### API errors and cross-origin access

- [ ] Add consistent error responses for invalid requests, missing resources, access-denied errors, and conflicts.
- [ ] Ensure Spring Security or API Gateway protects controller routes and rejects unauthorized requests.
- [ ] Configure CORS for the deployed frontend origin if it differs from the API origin.
