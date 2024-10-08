package cz.lastaapps.api

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.touchlab.kermit.Logger

class Store(
    config: AppConfig,
) {
    private val log = Logger.withTag("Store")

    private val database: Database = createDatabase(createDriver(config.databaseFileName))
    private val queries get() = database.schemaQueries

    fun storeAuthorizedPage(authorizedPage: AuthorizedPageFromUser) {
        log.d {
            "Storing authorized page ${authorizedPage.pageID} ${authorizedPage.pageName} " +
                "from user ${authorizedPage.userID} ${authorizedPage.userName}"
        }
        with(authorizedPage) {
            queries.insertAuthenticatedPage(pageID, pageName, pageAccessToken)
        }
    }

    fun loadAuthenticatedPages(): List<AuthorizedPage> =
        queries
            .selectAllPages()
            .executeAsList()
            .map { (pageID, pageName, pageAccessToken) ->
                AuthorizedPage(id = pageID, name = pageName, accessToken = pageAccessToken)
            }

    /**
     * Return discord channel ID and page access token of the pages related to the channel
     */
    fun loadPageDiscordPairs(): Map<String, List<AuthorizedPage>> =
        queries
            .selectChannelsWithPages()
            .executeAsList()
            .map {
                with(it) {
                    channel_id to AuthorizedPage(id = page_id, name = page_name, accessToken = page_access_token)
                }
            }.groupBy { it.first }
            .mapValues { (_, value) -> value.map { it.second } }

    fun createChannelPageRelation(
        channelID: String,
        pageID: String,
    ) {
        log.d { "Creating relation between channel $channelID and page $pageID" }
        queries.assignPageToDiscordChannel(channel_id = channelID, page_id = pageID)
    }

    fun removeChannelPageRelation(
        channelID: String,
        pageID: String,
    ) {
        log.d { "Removing relation between channel $channelID and page $pageID" }
        queries.removePageToDiscordChannel(channel_id = channelID, page_id = pageID)
    }

    fun createMessagePostRelation(
        channelID: String,
        messageID: String,
        postID: String,
    ) {
        log.d { "Creating relation between message $messageID and post $postID" }
        queries.assignMessageToPost(channel_id = channelID, message_id = messageID, post_id = postID)
    }

    fun getMessagePostRelations(): List<Pair<String, String>> =
        queries
            .selectMessagesWithPosts()
            .executeAsList()
            .map { it.message_id to it.post_id }

    fun getMessagesRelatedToPost(
        channelID: String,
        postID: String,
    ): List<String> =
        queries
            .selectMessagesForPost(channelID, postID)
            .executeAsList()
            .map { it.message_id }

    private fun createDatabase(driver: SqlDriver): Database = Database(driver)

    private fun createDriver(dbName: String): SqlDriver {
        log.d { "Creating sql driver from the DB \"$dbName\", schema version ${Database.Schema.version}" }
        val driver: SqlDriver =
            JdbcSqliteDriver(
                "jdbc:sqlite:$dbName",
                schema = Database.Schema,
            )
        return driver
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Store.isPostPosted(
    channelID: String,
    postID: String,
): Boolean = getMessagesRelatedToPost(channelID, postID).isNotEmpty()
