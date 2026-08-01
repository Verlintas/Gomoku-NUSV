package com.gomoku.nusv

/**
 * 平台算力上限：控制 AI 搜索深度。
 * 桌面端算力强可加深，手机端有限需克制。
 */
expect fun platformMaxAiDepth(): Int

/** AI 单步思考时间上限（毫秒）。超时则截断搜索并提示性能警告。 */
expect fun platformAiTimeLimitMs(): Long
