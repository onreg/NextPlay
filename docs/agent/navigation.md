## Navigation rules (app host)

Applies when editing app-level navigation in `app/src/main/kotlin/**` (symbols: `NavHost`, `NavController`, `composable(...)`, `navArgument`).

### Route definitions

- Centralize routes and argument keys in a single place (pattern: a `private object AppRoutes`).
- Keep argument keys as constants (`const val ARG_X = "x"`).
- Define routes with placeholders (`"Route/{$ARG_X}"`) and expose helper builders:
  - `fun route(x: Int): String = "Route/$x"`

### NavHost wiring

- Keep the `NavHost` in the app host (for example `MainActivity`).
- Provide navigation as callbacks into feature panes:
  - Pane takes `goBack`, `openDetails(id)`, etc.
  - Pane collects ViewModel events and invokes those callbacks.
  - App host converts callbacks into `nav.navigate(...)` / `nav.popBackStack()`.
- Do not pass `NavController`/`NavHostController` into ViewModels.

### Arguments

- Declare arguments using `navArgument(...) { type = ... }`.
- Read args from `backStackEntry.arguments` and handle missing values by exiting the destination early.

### Verification

After completing navigation changes, verify with this checklist:

- Routes and argument keys are centralized and have helper builders.
- Feature panes receive navigation as callbacks (no `NavController` leaks into feature/presentation layers).
- Destinations declare `navArgument` types and safely read arguments from `backStackEntry`.

