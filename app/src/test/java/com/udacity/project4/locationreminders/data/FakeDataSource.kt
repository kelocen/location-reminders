package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

/**
 * A fake data source that acts as a test double to the local data source.
 */
class FakeDataSource : ReminderDataSource {

    private var reminders: MutableList<ReminderDTO>? = mutableListOf()
    private var shouldReturnError: Boolean = false

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error(Exception("Reminder Exception!").toString())
        }
        reminders?.let { reminders ->
            for (reminder in reminders) {
                if (reminder.id == id) {
                    return Result.Success(reminder)
                }
            }
        }
        return Result.Error(Exception("Reminder not found!").toString())
    }

    override suspend fun deleteReminder(id: String) {
        reminders?.let { reminders ->
            reminders.removeIf { reminder -> (reminder.id == id) }
        }
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(Exception("Reminder Exception!").toString())
        }
        reminders?.let { reminders ->
            return Result.Success(ArrayList(reminders))
        }
        return Result.Error(Exception("Reminders not found!").toString())
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    override suspend fun getCount(): Int {
        return reminders!!.size
    }

    /**
     * Uses the given [Boolean] to specify if an error should be returned.
     */
    fun setShouldReturnError(returnError: Boolean) {
        shouldReturnError = returnError
    }
}