#! /usr/bin/env python
# :noTabs=true:
"""
create_update_statements.py

Author: Gijs Kant <gijs@thehyve.nl>
"""
import csv
import os
import sys
import StringIO

def read(in_s):
    result = []
    reader = csv.reader(in_s)
    for row in reader:
        if not len(row) == 4:
            print >> sys.stderr, "Expected row length of 4. Did you add the comment_order column?"
            sys.exit(1)
        result.append((int(row[0]), int(row[1]), int(row[2])))
    return result

def write(out_s, table_name, comment_id_column, item_id_column, order_column, values):
    current_item = None
    i = 0
    for (item_id, comment_id, comment_order) in values:
        if item_id <> current_item:
            current_item = item_id
            i = 0
        sql = "update {} set {} = {} where {} = {} and {} = {};".format(
            table_name, order_column, i, item_id_column, item_id, comment_id_column, comment_id)
        #if i <> comment_order:
            #print >> sys.stderr, "!!! i = {}, comment_order = {}".format(i, comment_order)
        out_s.write(sql)
        out_s.write("\n")
        i = i + 1

def main():
    if len(sys.argv) < 6:
        print >> sys.stderr, ("Usage: %s <in> <tablename> <comment_id_column> <item_id_column> <order_column> [out]"
            % os.path.basename(sys.argv[0]))
        sys.exit()

    in_file = sys.argv[1]
    table_name = sys.argv[2]
    comment_id_column = sys.argv[3]
    item_id_column = sys.argv[4]
    order_column = sys.argv[5]
    out_file = 'stdout'
    write_to_stdout = True
    if len(sys.argv) > 6:
        out_file = sys.argv[6]
        write_to_stdout = False

    result = None
    print >> sys.stderr, "Reading comment ids from %s ..." % in_file
    with open(in_file) as s_in:
        result = read(s_in)

    if result is None:
        print >> sys.stderr, "Reading comment ids failed."
        sys.exit(1)

    if write_to_stdout:
        s_out = sys.stdout
    else:
        s_out = open(out_file, 'w')
    print >> sys.stderr, "Writing update statements to %s ..." % out_file
    write(s_out, table_name, comment_id_column, item_id_column, order_column, result)

    if not write_to_stdout:
        s_out.close()

    print >> sys.stderr, "Done."


if __name__ == '__main__':
    main()

