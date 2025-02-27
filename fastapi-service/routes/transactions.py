from datetime import datetime,timezone
from fastapi import APIRouter, HTTPException,Query
from database import transactions_collection
from bson import ObjectId, Decimal128
from typing import List


router = APIRouter()

def convert_objectid(data):
    """Convert ObjectId and Decimal128 to string or float recursively"""
    if isinstance(data, list):
        return [convert_objectid(doc) for doc in data]
    elif isinstance(data, dict):
        return {
            k: (float(v.to_decimal()) if isinstance(v, Decimal128) else str(v) if isinstance(v, ObjectId) else v)
            for k, v in data.items()
        }
    return data

@router.get("/list-transactions")
async def list_transactions():
    try:
        transactions = await transactions_collection.find().to_list(100)
        transactions = convert_objectid(transactions)
        return {"count": len(transactions), "transactions": transactions}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/transactions-between-dates")
async def get_transactions_by_date_range(
    start_date: str = Query(..., description="Start date in format YYYY-MM-DD"),
    end_date: str = Query(..., description="End date in format YYYY-MM-DD")
):
    try:
        # Convert input strings to UTC datetime objects
        start_datetime = datetime.strptime(start_date, "%Y-%m-%d").replace(tzinfo=timezone.utc)
        end_datetime = datetime.strptime(end_date, "%Y-%m-%d").replace(tzinfo=timezone.utc)

        # Ensure end_datetime covers the whole day (set time to 23:59:59.999)
        end_datetime = end_datetime.replace(hour=23, minute=59, second=59, microsecond=999999)

        # Query MongoDB for transactions within the date range
        transactions = await transactions_collection.find(
            {"timestamp": {"$gte": start_datetime, "$lte": end_datetime}}
        ).to_list(100)

        transactions = convert_objectid(transactions)
        return {"count": len(transactions), "transactions": transactions}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))