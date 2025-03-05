import os
from fastapi import FastAPI
from pymongo import MongoClient
import redis
from routes import transactions, reports, anomaly

app = FastAPI()

# Include routers
app.include_router(transactions.router, prefix="/transactions", tags=["Transactions"])
app.include_router(reports.router, prefix="/reports", tags=["Reports"])
app.include_router(anomaly.router, prefix="/anomaly", tags=["Anomaly"])


mongo_uri = os.getenv("MONGODB_URI", "mongodb://mongodb:27017/digital-wallet-system")
mongo_client = MongoClient(mongo_uri)
db = mongo_client.get_database()

# Redis setup
redis_client = redis.Redis(
    host=os.getenv("REDIS_HOST", "localhost"),
    port=int(os.getenv("REDIS_PORT", 6379)),
    decode_responses=True
)

@app.get("/")
async def root():
    return {"message": "FastAPI Transaction Analytics Service is Running"}

@app.get("/health")
def health_check():
    try:
        # Mongo check
        mongo_client.admin.command('ping')
        # Redis check
        redis_client.ping()
        return {"status": "up"}
    except Exception as e:
        return {"status": "down", "error": str(e)}
