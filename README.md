# Havatime
HavaTime is an Android application that helps people to pick the best shuttle time to go to the airports in Istanbul. It uses 
Google Distance Matrix APIs to make the best guesses.

Travel time is obtained by sending a request to the API and parsing the JSON.

The application suggests the user three shuttles with their risk of missing the flight. Risk is obtained by first calculating the time user will have at the airport and then it is proportioned with the constants as maximum time at the airport and minimum time at the airport.

Trace
