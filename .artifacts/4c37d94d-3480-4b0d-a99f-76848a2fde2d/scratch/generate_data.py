import json
import uuid
import time
from datetime import datetime, timedelta

def generate_data():
    now = datetime(2026, 8, 15, 2, 51)
    start_date = now - timedelta(days=30)
    
    exercises = []
    meals = []
    weights = []
    
    current_date = start_date
    day_count = 0
    
    while current_date <= now:
        timestamp = int(current_date.timestamp() * 1000)
        
        # Meals (3 per day)
        meals.append({
            "id": str(uuid.uuid4()),
            "date": timestamp + (8 * 3600 * 1000), # 8 AM
            "mealName": "Healthy Breakfast",
            "calories": 450,
            "protein": 20.0,
            "carbs": 55.0,
            "fat": 12.0,
            "notes": "Oats and fruit"
        })
        meals.append({
            "id": str(uuid.uuid4()),
            "date": timestamp + (13 * 3600 * 1000), # 1 PM
            "mealName": "Chicken and Rice",
            "calories": 650,
            "protein": 40.0,
            "carbs": 60.0,
            "fat": 15.0,
            "notes": "Standard meal prep"
        })
        meals.append({
            "id": str(uuid.uuid4()),
            "date": timestamp + (19 * 3600 * 1000), # 7 PM
            "mealName": "Steak and Veggies",
            "calories": 700,
            "protein": 50.0,
            "carbs": 10.0,
            "fat": 40.0,
            "notes": "High protein dinner"
        })
        
        # Exercises (every other day)
        if day_count % 2 == 0:
            exercises.append({
                "id": str(uuid.uuid4()),
                "date": timestamp + (17 * 3600 * 1000), # 5 PM
                "exerciseName": "Push Day" if day_count % 4 == 0 else "Pull Day",
                "sets": [
                    { "id": str(uuid.uuid4()), "weight": "80", "reps": "10", "unit": "KG" },
                    { "id": str(uuid.uuid4()), "weight": "80", "reps": "10", "unit": "KG" },
                    { "id": str(uuid.uuid4()), "weight": "80", "reps": "8", "unit": "KG" }
                ],
                "notes": "Consistent effort",
                "caloriesBurned": 300
            })
            
        # Weights (every 3 days)
        if day_count % 3 == 0:
            weights.append({
                "id": str(uuid.uuid4()),
                "date": timestamp + (7 * 3600 * 1000), # 7 AM
                "weightKg": 80.0 - (day_count * 0.1) # Slowly decreasing
            })
            
        current_date += timedelta(days=1)
        day_count += 1
        
    backup_data = {
        "exercises": exercises,
        "meals": meals,
        "weights": weights
    }
    
    with open('/Users/adwait/Gym-Logs-android/.artifacts/4c37d94d-3480-4b0d-a99f-76848a2fde2d/scratch/monthly_backup.json', 'w') as f:
        json.dump(backup_data, f, indent=2)

if __name__ == "__main__":
    generate_data()
