from __future__ import annotations

from collections import defaultdict
from dataclasses import dataclass, field
from datetime import datetime
from typing import Dict, List

from apscheduler.schedulers.background import BackgroundScheduler
from fastapi import FastAPI, File, Form, UploadFile
from pydantic import BaseModel, Field

app = FastAPI(title="Consthinking Backend", version="0.1.0")
scheduler = BackgroundScheduler()


@dataclass
class UserContext:
    notes_by_day: Dict[str, List[str]] = field(default_factory=lambda: defaultdict(list))
    interests: List[str] = field(default_factory=list)
    summary_time: str = "21:30"
    news_push_time: str = "07:30"


USER_STORE: Dict[str, UserContext] = defaultdict(UserContext)


class SettingsUpdate(BaseModel):
    user_id: str
    interests: List[str] = Field(default_factory=list, max_length=3)
    summary_time: str = "21:30"
    news_push_time: str = "07:30"
    glm_api_base: str | None = None
    glm_api_key: str | None = None


@app.on_event("startup")
def startup() -> None:
    scheduler.add_job(push_daily_summary, "cron", hour=21, minute=30, id="daily-summary")
    scheduler.add_job(push_weekly_summary, "cron", day_of_week="sun", hour=21, minute=40, id="weekly-summary")
    scheduler.add_job(push_monthly_summary, "cron", day="last", hour=21, minute=50, id="monthly-summary")
    scheduler.add_job(refresh_navigation_catalog, "cron", hour="7,19", minute=0, id="catalog-refresh")
    scheduler.add_job(push_newsletter_and_podcast, "cron", hour=7, minute=30, id="news-podcast")
    scheduler.start()


@app.on_event("shutdown")
def shutdown() -> None:
    scheduler.shutdown(wait=False)


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/settings")
def update_settings(payload: SettingsUpdate) -> dict:
    user = USER_STORE[payload.user_id]
    user.interests = payload.interests[:3]
    user.summary_time = payload.summary_time
    user.news_push_time = payload.news_push_time
    # TODO: 在此保存 glm_api_base/glm_api_key（建议改为加密存储）
    return {"message": "settings updated", "interests": user.interests}


@app.post("/reflection/audio")
async def upload_audio(
    user_id: str = Form(...),
    ts: str = Form(default=""),
    audio: UploadFile = File(...),
) -> dict:
    day_key = datetime.now().strftime("%Y-%m-%d")
    USER_STORE[user_id].notes_by_day[day_key].append(f"audio:{audio.filename} @ {ts}")
    # TODO: 1) 存对象存储 2) 语音转写 3) 将内容写入当天上下文 4) 调用 GLM 进行摘要
    return {"message": "已保存", "day": day_key}


@app.post("/reflection/screenshot")
async def upload_screenshot(
    user_id: str = Form(...),
    ts: str = Form(default=""),
    screenshot: UploadFile = File(...),
) -> dict:
    day_key = datetime.now().strftime("%Y-%m-%d")
    USER_STORE[user_id].notes_by_day[day_key].append(f"screenshot:{screenshot.filename} @ {ts}")
    # TODO: 1) 图像 OCR/视觉理解 2) 结果写入当天上下文 3) 调用 GLM
    return {"message": "已保存", "day": day_key}


@app.get("/summary/today/{user_id}")
def get_today_context(user_id: str) -> dict:
    day_key = datetime.now().strftime("%Y-%m-%d")
    return {"day": day_key, "events": USER_STORE[user_id].notes_by_day[day_key]}


def push_daily_summary() -> None:
    for user_id, ctx in USER_STORE.items():
        day_key = datetime.now().strftime("%Y-%m-%d")
        events = ctx.notes_by_day.get(day_key, [])
        # TODO: 调用 GLM 进行“当日反思 + 建议”总结，然后通过推送渠道发送给用户
        print(f"[DAILY] user={user_id}, events={len(events)}")


def push_weekly_summary() -> None:
    # TODO: 汇总最近 7 天上下文，生成周报
    print("[WEEKLY] run weekly summary")


def push_monthly_summary() -> None:
    # TODO: 汇总本月上下文，生成月报
    print("[MONTHLY] run monthly summary")


def push_newsletter_and_podcast() -> None:
    # TODO: 根据用户兴趣抓取新闻，生成电子报与播客音频
    print("[NEWS] run newsletter + podcast job")


def refresh_navigation_catalog() -> None:
    # TODO: 早7晚7更新目录导航（资讯、榜单短视频、小程序游戏、优惠羊毛）
    print("[CATALOG] refresh catalog links")
