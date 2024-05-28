CREATE INDEX idx_stops_stop_id ON stops (stop_id);

CREATE INDEX idx_routes_route_id ON routes (route_id);

CREATE INDEX idx_trips_trip_id ON trips (trip_id);
CREATE INDEX idx_trips_route_id ON trips (route_id);

CREATE INDEX idx_stop_times_trip_id ON stop_times (trip_id);
CREATE INDEX idx_stop_times_stop_id ON stop_times (stop_id);
CREATE INDEX idx_stop_times_departure_time ON stop_times (departure_time);
CREATE INDEX idx_stop_times_arrival_time ON stop_times (arrival_time);
