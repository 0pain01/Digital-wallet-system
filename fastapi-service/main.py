from fastapi import FastAPI
from routes import transactions, reports, anomaly

app = FastAPI()

# Include routers
app.include_router(transactions.router, prefix="/transactions", tags=["Transactions"])
app.include_router(reports.router, prefix="/reports", tags=["Reports"])
app.include_router(anomaly.router, prefix="/anomaly", tags=["Anomaly"])


@app.get("/")
async def root():
    return {"message": "FastAPI Transaction Analytics Service is Running"}
