#!/bin/bash

if [ "$#" -lt 2 ]; then
    echo "Usage: ${0} <table_name> <comment_id_column> <item_id_column>"
    echo "E.g., ${0} lab_request_comments comments_id lab_request_id"
    exit 1
fi

table_name="$1"
comment_id_column="$2"
item_id_column="$3"

sudo -u postgres psql -d dntp_portal -c "SELECT items.*, c.time_created FROM ${table_name} items JOIN comment c ON items.${comment_id_column} = c.id order by items.${item_id_column}, c.time_created ASC" -t -F "," -A

