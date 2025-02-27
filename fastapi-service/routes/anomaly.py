import redis
from fastapi import APIRouter, HTTPException

router = APIRouter()

# Connect to Redis
redis_client = redis.Redis(host="localhost", port=6379, decode_responses=True)

@router.get("/check-anomaly/")
async def check_anomaly(email: str):
    """Check if a user has exceeded login failures"""
    failed_attempts = redis_client.get(f"failedAttempts:{email}")
    
    if failed_attempts is None:
        return {"email": email, "message": "No failed attempts detected"}
    
    failed_attempts = int(failed_attempts)
    
    if failed_attempts >= 3:
        return {"email": email, "message": "Anomaly Detected! Multiple failed login attempts."}
    
    return {"email": email, "message": f"Failed attempts: {failed_attempts}"}
