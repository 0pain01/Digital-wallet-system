from pydantic import BaseModel
from datetime import datetime
from typing import Optional

class Transaction(BaseModel):
    senderId: str
    receiverId: str
    amount: float
    type: str  # TRANSFER, WITHDRAW, DEPOSIT
    timestamp: Optional[datetime] = datetime.utcnow()
