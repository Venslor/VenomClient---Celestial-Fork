package net.pvproom.client.event.impl

import net.pvproom.client.event.Event
import net.pvproom.client.utils.lunar.CrashReportResult

class CrashReportUploadEvent(val result: CrashReportResult) : Event()
