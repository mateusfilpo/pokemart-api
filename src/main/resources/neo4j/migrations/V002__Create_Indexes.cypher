CREATE INDEX item_deleted_index IF NOT EXISTS FOR (i:Item) ON (i.deleted);

CREATE INDEX item_price_index IF NOT EXISTS FOR (i:Item) ON (i.price);

CREATE INDEX item_normalized_search_index IF NOT EXISTS FOR (i:Item) ON (i.normalizedSearch);

CREATE INDEX user_email_index IF NOT EXISTS FOR (u:User) ON (u.email);
