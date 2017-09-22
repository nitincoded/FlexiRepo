# FlexiRepo

FlexiRepo acts as an HTTP gateway to the database by using JDBC drivers. You can now connect 
to non-FOSS databases (Eg. MS SQL, Oracle) from FOSS platforms (PHP, Python, Ruby) without
having to look for a driver.

For ensuring security, avoid making HTTP calls to FlexiRepo directly from a desktop or mobile app.

## Usage

Send an HTTP request to /query or /execute with the jdbcUrl query string parameter with the JDBC URL to the database and the sql query statement parameter with the SQL statement.

To get the results of an SQL query, send a GET request to /query

To execute an SQL query without fetching results, send a GET or POST request to /execute

## Example

Ruby code snippet

```
s_uenc_jdbc = CGI.escape('jdbc:mysql://127.0.0.1/mydb?useLegacyDatetimeCode=false%26serverTimezone=Asia/Dubai%26user=root%26password=donttellanyone')
s_uenc_sql = CGI.escape('SELECT * FROM chocolate')

s_url = "http://127.0.0.1:2020/query?jdbcUrl=#{s_uenc_jdbc}&sql=#{s_uenc_sql}"

uri = URI.parse(s_uri)
response = Net::HTTP.get_response uri

echo JSON.parse(response.body)
```

## Other Notes

When connecting to an Oracle database, the JDBC thin driver is preferred for portability across platforms.
