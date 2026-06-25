# Training-Nutrition-android

A minimal, focused Android app to track daily workouts and meals — built in Kotlin. Log exercises (sets, weight, reps) and meals (calories + optional macros) quickly with a clean card-based UI.

Status: Draft — screenshots provided by the author.

---

## Table of contents
- Features
- Screenshots
- Tech stack
- Getting started
- Usage
- Project structure (high level)
- Contributing
- Roadmap
- License
- Contact

---

## Features
- Add / edit / delete exercises (sets with weight & reps)
- Add / edit / delete meals (required calories; optional macros: protein, carbs, fat)
- Daily views for Workouts and Meals with simple cards
- Notes fields for both exercises and meals
- Date picker and dashboard summary with nutrition totals
- Light/dark mode toggle (UI supports switching)
- Import / export and clear data options in Settings

---

## Screenshots
Place your screenshots in `app/src/main/assets/screenshots/` (recommended). Currently the images in this repository live at `app/src/main/app/src/main/assets/screenshots/` so the README below uses those paths so images render correctly on GitHub. Consider moving the image files later to `app/src/main/assets/screenshots/` to clean up the directory structure.

![Add Meal](app/src/main/app/src/main/assets/screenshots/01_add_meal.jpg)  
Shows the Add Meal form: meal name, previous meal reference, required calories, optional macros (Protein/Carbs/Fat), and notes field.

![Add Exercise](app/src/main/app/src/main/assets/screenshots/02_add_exercise.jpg)  
Shows the Add Exercise form: exercise name, previous session reference, sets (lbs & reps), and notes.

![Meals view](app/src/main/app/src/main/assets/screenshots/03_meals_view.jpg)  
Daily meal card listing with calories and macro chips (edit/delete icons present).

![Workouts view](app/src/main/app/src/main/assets/screenshots/04_workouts_view.jpg)  
Daily workout cards listing sets (weight × reps) and actions.

![Dashboard](app/src/main/app/src/main/assets/screenshots/05_dashboard.jpg)  
The Dashboard with date picker, quick search, Today's Workouts, and Nutrition Tracker summary.

![Meal History](app/src/main/app/src/main/assets/screenshots/06_meal_history.jpg)  
Historical list of a specific meal across dates with macro chips and edit/delete actions.

![Settings](app/src/main/app/src/main/assets/screenshots/07_settings_menu.jpg)  
Settings menu showing Dark Mode toggle, Import/Export Data, and Clear All Data.

