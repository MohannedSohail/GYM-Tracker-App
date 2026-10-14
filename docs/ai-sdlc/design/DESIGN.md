# DESIGN: Terra Design System for "جيم" (Gym Workout Logging)

- **Source**: Stitch MCP Project `projects/14795988958690646993` ("Arabic Shared Workout Tracker")
- **Spec Reference**: [SPEC-001](file:///e:/AI%20Projects/GYM%20Tracker%20App/docs/ai-sdlc/specs/SPEC-001.md)
- **Target device**: Google Pixel 7 Pro, 412 x 892 dp, portrait
- **Status**: active

---

## 1. Design Principles
1. **Calm and simple**: warm off-white surfaces, one sage-green accent, sand-gold used only as a decoration.
2. **One light theme only**: no dark fills, no black bars, no gradients, no neon. Darkest element = text.
3. **Used mid-workout**: tired, one-handed, sweaty hands. Min tap target 56dp, steppers 64dp, main actions in the bottom third.
4. **Numbers are the hero**: weight and reps at 56sp bold.
5. **Logging one round = 3 taps or fewer** (pre-filled values, then tap "تسجيل الجولة").
6. **Show only what is required**: no stats, banners, tips, decorative widgets or charts.

---

## 2. Platform Rules
- **Arabic only, full RTL** (`dir="rtl"`, text right-aligned, directional icons mirrored, back arrow points right).
- **Digits**: Western `0-9`, tabular figures.
- **Light theme only** in v1.

---

## 3. Color Tokens
| Token | Hex | Usage |
|---|---|---|
| `primary` | `#4A7C59` | Primary buttons, selected state |
| `onPrimary` | `#FFFFFF` | Text/icons on primary |
| `primaryContainer` | `#CDEBD3` | Stepper buttons, selected cards/chips |
| `onPrimaryContainer` | `#12331F` | Text/icons on primaryContainer |
| `secondary` | `#6E655A` | Secondary icons and text |
| `onSecondary` | `#FFFFFF` | Text on secondary |
| `secondaryContainer` | `#EAE2D8` | Secondary buttons |
| `onSecondaryContainer` | `#2A231B` | Text on secondary buttons |
| `tertiary` | `#C9A962` | Decorative only (accent line, future badge). Never text on white |
| `onTertiary` | `#3A2D0C` | Text on tertiary |
| `tertiaryContainer` | `#F4E7C5` | Subtle warm tint |
| `background` | `#F7F6F2` | Screen background |
| `surface` | `#FFFFFF` | Cards, dialogs, bottom bars |
| `surfaceVariant` | `#EDEEE8` | Input fields, unselected pills/chips |
| `outline` | `#D9DBD2` | 1dp borders, dividers |
| `onSurface` (textPrimary) | `#1B2420` | Titles, numbers, body |
| `onSurfaceVariant` (textSecondary) | `#5A645E` | Captions, hints |
| `error` | `#BA3B2E` | Delete, errors |
| `errorContainer` | `#FBE6E2` | Error message background |

### Friend avatar palette (initial letter in a circle)
| # | Background | Letter |
|---|---|---|
| 1 | `#CDEBD3` | `#1F4A30` |
| 2 | `#F4E7C5` | `#5C4510` |
| 3 | `#E6DFD4` | `#4A4036` |

---

## 4. Typography
Font family: **IBM Plex Sans Arabic** (system fallback: Cairo, sans-serif).
- **Display Number**: 56sp / Bold (weight and reps)
- **Title Large**: 22sp / SemiBold (screen titles)
- **Title Medium**: 18sp / SemiBold (card titles, headers)
- **Body Large**: 16sp / Regular / SemiBold (body, buttons)
- **Body Small / Caption**: 13sp / Regular (hints, captions, tags)

---

## 5. Shape, Spacing, Elevation
- **Card radius**: 20dp
- **Button radius**: 16dp
- **Chip / tag radius**: 12dp
- **Stepper buttons**: 64dp circular
- **Dialog radius**: 24dp
- **Screen margin**: 20dp
- **Grid**: 8dp
- **Card elevation**: 1-2dp soft shadow + 1dp outline border `#D9DBD2`
- **Icons**: Material Symbols Rounded, 24dp

---

## 6. Screen Inventory (Stitch Screens)
1. `01 Splash` (`4a66d0bc3a1b4baeab420bd008d17c5e`): App logo & branding
2. `02 FirstRunSetup` (`2b5f4921c5aa4ce28b3aea8133a9e37a`): Initial group member setup
3. `03 Home` (`241acba40b39437eb9d5265b0552bd97`): Start session / Active session banner
4. `04 ActiveSession` (`e4c5161c2adc40c886f9460cab2d6436`): Real-time set logging with steppers
5. `05 History` (`0e7a7da7832c41449ff9fc92a6211cb5`): Chronological session history
6. `06 SessionDetail` (`a788ecabcc5a4178abf0134f7714d064`): Detailed set breakdowns
7. `07 Friends` (`1bfec284ad7b47ce819793be1a439dba`): Participant list management
8. `08 ExercisePicker` (`72f6226b787643bebdc832146d85df49`): 30 categorized exercises
9. `d5 AddEditFriendDialog` (`b849363b56984e919b7a1043ec8d9af5`): Add / Edit participant dialog
10. `d6 DeleteFriendDialog` (`6bb795fd67774046a0f1c368bd2ab9b0`): Soft-delete confirmation dialog
