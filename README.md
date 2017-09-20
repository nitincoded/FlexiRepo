# FlexiRepo

FlexiRepo acts as an HTTP gateway to the database by using JDBC drivers. You can now connect 
to non-FOSS databases (Eg. MS SQL, Oracle) from FOSS platforms (PHP, Python, Ruby) without
having to look for a driver.

## Usage

Send an HTTP request with the JDBC URL to the database and SQL statement.

To get the results of an SQL query, send a GET request to /query

To execute an SQL query without fetching results, send a GET or POST request to /execute

## Example

```
s_uenc_jdbc = CGI.escape('jdbc:mysql://127.0.0.1/mydb?useLegacyDatetimeCode=false%26serverTimezone=Asia/Dubai%26user=root%26password=donttellanyone')
s_uenc_sql = CGI.escape('SELECT * FROM chocolate')
s_url = "http://127.0.0.1:2020/query?jdbcUrl=#{s_uenc_jdbc}&sql=#{s_uenc_sql}"
uri = URI.parse(s_uri)
response = Net::HTTP.get_response uri
echo JSON.parse(response.body)
```
