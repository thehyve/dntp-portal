#!/bin/bash

csv_dir="csv/"
sql_dir="sql/"

mkdir "${csv_dir}"
mkdir "${sql_dir}"

update_table() {
    table_name="$1"
    comment_id_column="$2"
    item_id_column="$3"
    order_column="$4"
    ./read_comment_ids.sh "${table_name}" "${comment_id_column}" "${item_id_column}" > "${csv_dir}${table_name}.csv" && {
        ./create_update_statements.py "${csv_dir}${table_name}.csv" "${table_name}" "${comment_id_column}" "${item_id_column}" "${order_column}" > "${sql_dir}${table_name}.sql"
        return $?
    }
    return 1
}

update_table "lab_request_comments" "comments_id" "lab_request_id" "comments_order" || {
    echo "Error!"
    exit 1
}

update_table "request_properties_comments" "comments_id" "request_properties_id" "comments_order" || {
    echo "Error!"
    exit 1
}

update_table "request_properties_approval_comments" "approval_comments_id" "request_properties_id" "approval_comments_order" || {
    echo "Error!"
    exit 1
}

