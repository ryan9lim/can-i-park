from sodapy import Socrata
import geocoder
import json
import math

client = Socrata("data.cityofnewyork.us", None)

def valid_query_street_part(s):
    NotName = ['West', 'North', 'South', 'East', 'Ave', 'St', 'Street', 'Avenue']
    if len(s) < 3:
        return False
    if s in NotName:
        return False
    return True

def query(lat, lng):
    g = geocoder.google([lat, lng], method='reverse')

    where = "(" + " or ".join(
        ["street_name = '"+st+"'"
        for st in g.street.split() if valid_query_street_part(st)]
    ) + ")"

    st_number = 0
    try:
        st_number = int(g.street_number.split("-")[0])
    except ValueError:
        st_number = 0

    order = 'house_number DESC'

    results = client.get("kiv2-tbus", where=where, order=order)

    if len(results) == 0:
        return 0

    count = 0.0
    for r in results:
        try:
            if abs(int(r['house_number']) - st_number) <= 50:
                count += 1
            else:
                count += 1 / ( 1 + abs(int(r['house_number']) - st_number) - 50 )
        except (ValueError, KeyError) as e:
            pass

    print(len(results))

    return  6 * math.sqrt(count) / 100
