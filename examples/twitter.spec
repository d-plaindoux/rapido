//
// Source https://dev.twitter.com/docs/api/1.1
//

type Timeline = {
    count: Int,
    since_id: String,
    max_id: String,
    trim_user: Bool
}

type MentionsTimeline = Timeline with {
    contributor_details: Bool,
    include_entities: Bool
}

type UserTimeline = Timeline with {
    user_id: String?,
    screen_name: String?,
    exclude_replies: String?,
    contributor_details: Bool,
    include_rts: String?
}

type HomeTimeline = Timeline with {
    contributor_details: Bool,
    exclude_replies: String?
}

type RetweetsOfMe = Timeline with {
    include_entities: Bool,
    include_user_entities: Bool
}

type Error = {}
type Empty = {}
type Any = {}

service timelines {
    mentions_timeline: MentionsTimeline => Any* or Error = GET[mentions_timeline.json] PARAMS[MentionsTimeline] BODY[Empty]
    user_timeline: UserTimeline => Any* or Error = GET[user_timeline.json] PARAMS[UserTimeline] BODY[empty]
    home_timeline: HomeTimeline => Any* or Error = GET[home_timeline.json] PARAMS[HomeTimeline] BODY[empty]
    retweets_of_me: RetweetsOfMe => Any* or Error = GET[retweets_of_me.json] PARAMS[RetweetsOfMe] BODY[empty]
}

route timelines [statuses]

client twitterRest provides timelines
