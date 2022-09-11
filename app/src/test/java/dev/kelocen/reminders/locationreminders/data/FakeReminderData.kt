package dev.kelocen.reminders.locationreminders.data

import dev.kelocen.reminders.locationreminders.data.dto.ReminderDTO
import dev.kelocen.reminders.locationreminders.reminderslist.ReminderDataItem
import java.io.Serializable
import java.util.*

/**
 * A data class for fake reminders.
 */
data class ReminderTestItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    val id: String = UUID.randomUUID().toString()
) : Serializable

/**
 * A [MutableList] of [ReminderTestItem] items.
 */
val fakeReminderData: MutableList<ReminderTestItem> = mutableListOf(
    ReminderTestItem(
        "Texas Souvenir",
        "Get a souvenir from Texas Capitol.",
        "1100 Congress Ave, Austin, TX 78701",
        30.26440196397559,
        -97.73692867466714
    ),
    ReminderTestItem(
        "California Souvenir",
        "Souvenir from Golden Gate Bridge.",
        "Golden Gate Bridge, San Francisco, CA",
        37.820098082571974,
        -122.47820145749705
    ),
    ReminderTestItem(
        "Colorado Souvenir",
        "Take photos at Peak to Peak Scenic Byway.",
        "72 CO-7, Estes Park, CO 80517",
        40.37533250714026,
        -105.50908443286993
    ),
    ReminderTestItem(
        "Arizona Souvenir",
        "Souvenir from Meteor Crater Landmark.",
        "Interstate 40, Exit, 233, Winslow, AZ 86047",
        35.027868592531796,
        -111.02145311209803
    ),
    ReminderTestItem(
        "Alaska Souvenir",
        "Take photos at Aniakchak Crater.",
        "King Salmon, AK 99613",
        56.89678600912497,
        -158.08588720761222
    ),
    ReminderTestItem(
        "New York Souvenir",
        "Get souvenir at Statue of Liberty.",
        "New York, NY 10004",
        40.68943648883334,
        -74.04414635071107
    ),
    ReminderTestItem(
        "South Dakota Souvenir",
        "Get souvenir at Mount Rushmore.",
        "13000 SD-244, Keystone, SD 57751",
        43.87938088030983,
        -103.45890576964679
    ),
)

/**
 * A function to convert [ReminderTestItem] items to [ReminderDTO] items.
 */
fun List<ReminderTestItem>.asReminderDTO(): MutableList<ReminderDTO> {
    return map { reminder ->
        ReminderDTO(
            title = reminder.title,
            description = reminder.description,
            location = reminder.location,
            latitude = reminder.latitude,
            longitude = reminder.longitude
        )
    }.toMutableList()
}

/**
 * A function to convert [ReminderTestItem] items to [ReminderDataItem] items.
 */
fun List<ReminderTestItem>.asReminderDataItem(): MutableList<ReminderDataItem> {
    return map { reminder ->
        ReminderDataItem(
            title = reminder.title,
            description = reminder.description,
            location = reminder.location,
            latitude = reminder.latitude,
            longitude = reminder.longitude
        )
    }.toMutableList()
}