# Digital Contact API [DEPRECATED]

A Scala-based REST API service for managing digital contact preferences and secure messaging within the HMRC ecosystem.

## Overview

The Digital Contact API provides endpoints for:
- Retrieving secure message counts for users
- Managing digital communication preferences (paperless settings)

## API Endpoints

### Message Count
```
GET /messages/count
```
Returns the total and unread counts of secure messages for a user.

**Query Parameters:**
- `taxIdentifiers` (optional): Filter by specific tax identifiers (e.g., "nino")
- `regimes` (optional): Filter by tax regimes (e.g., "paye")

**Response:**
```json
{
  "count": {
    "total": 4,
    "unread": 1
  }
}
```

### User Preferences
```
GET /paperless/preferences
```
Returns the user's digital communication preferences.

**Query Parameters:**
- `returnUrl` (required): Encoded URL for redirecting back to the originating application
- `returnLinkText` (required): Encoded label for the return button

**Response:**
```json
{
  "digital": false,
  "category": "NewCustomer",
  "redirectUrl": "/paperless/choose/56?returnUrl=MAxGz4ddvlu94xTumMrbY6U0JEKxTW8xAK6Xu9Zw0D0LI3zHkmxOiq4O1uOCzLFq&returnLinkText=tGvtoo%2BQxasPY8ckzo4z2w%3D%3D"
}
```

### Running the Service
```bash
sbt run 9059
```

### Running Tests
```bash
sbt test
```
## Error Handling

The API returns standard HTTP status codes:

- **200**: Success
- **401**: Unauthorized - Invalid or missing bearer token
- **500**: Internal Server Error

### Building
```bash
sbt clean compile
```

## API Documentation

The full OpenAPI 3.0.3 specification is available in `application.yaml`.