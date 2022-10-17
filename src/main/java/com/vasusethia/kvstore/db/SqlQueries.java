package com.vasusethia.kvstore.db;

public class SqlQueries {

  public static final String GET = "SELECT * FROM STORE WHERE key=$1 AND ttl > EXTRACT(EPOCH FROM NOW())";

  //using this as a soft delete by setting ttl to -1 instead of deleting it, this will prevent tree re-balance
  //later we can use a cron job to do a batch delete
  public static final String DELETE = "UPDATE STORE SET TTL=-1 WHERE key=$1";

  //using upsert here instead of insert as this would add a key if the key doesn't exist as well as update if the key exist
  // we can apply update operation with a read-modify cycle but that would require me to wrap it around the transaction and take a lock
  // upsert can be used to update the old value if it exists or insert a new value
  public static final String PUT = "INSERT INTO STORE VALUES($1, $2, $3) ON CONFLICT(key) DO UPDATE SET value=$2, ttl=$3";

  public static final String CLEANUP_DELETE = "DELETE FROM STORE WHERE ttl < EXTRACT(EPOCH FROM NOW())";
}
