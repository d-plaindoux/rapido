//
// Source https://dev.twitter.com/docs/api/1.1
//

type Timeline = {
    count: int,
    since_id: string,
    max_id: string,
    trim_user: bool
}

type MentionsTimeline = Timeline with {
    contributor_details: bool,
    include_entities: bool
}

type UserTimeline = Timeline with {
    user_id: string?,
    screen_name: string?,
    exclude_replies: string?,
    contributor_details: bool,
    include_rts: string?
}

type HomeTimeline = Timeline with {
    contributor_details: bool,
    exclude_replies: string?
}

type RetweetsOfMe = Timeline with {
    include_entities: bool,
    include_user_entities: bool
}

type Error = {}
type Empty = {}
type Any = {}*

service timelines {
    mentions_timeline: MentionsTimeline => Any or Error = GET[mentions_timeline.json] PARAMS[MentionsTimeline]
    user_timeline: UserTimeline => Any or Error = GET[user_timeline.json] PARAMS[UserTimeline]
    home_timeline: HomeTimeline => Any or Error = GET[home_timeline.json] PARAMS[HomeTimeline]
    retweets_of_me: RetweetsOfMe => Any or Error = GET[retweets_of_me.json] PARAMS[RetweetsOfMe]
}

route timelines [statuses]

client twitterRest provides timelines
