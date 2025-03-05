from motor.motor_asyncio import AsyncIOMotorClient

MONGO_URI = "mongodb://mongodb:27017/digital-wallet-system"  # Remove extra slash
client = AsyncIOMotorClient(MONGO_URI)

# Select the correct database
db = client["Wallet"]  # Database name should match exactly


# Collections inside Wallet
transactions_collection = db["transactions"]  # Transactions Collection
users_collection = db["users"]  # Users Collection
wallets_collection = db["wallets"]  # Wallets Collection


