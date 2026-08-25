from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from api.routes import router


app = FastAPI(
    title="Vistaar Setu AI Backend",
    version="1.0.0"
)

app.mount(
    "/audio",
    StaticFiles(directory="generated_audio"),
    name="audio"
)

app.include_router(router)


@app.get("/health")
def health():
    return {
        "status": "ok"
    }