type MentionsTimeline = {
     count: Int,
     since_id: String, 
     max_id: String, 
     trim_user: Bool, 
     contributor_details: Bool, 
     include_entities: Bool
}

type UserTimeline = {}
type HomeTimeline = {}
type RetweetsOfMe = {}
type Error = {}

service timelines {
    mentions_timeline: MentionsTimeline => {} or Error = GET[mentions_timeline.json]
}

route timelines [statuses]

client twitterRest provides timelines
