/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {setGlobalOptions} from "firebase-functions";
import {onDocumentWritten} from 'firebase-functions/v2/firestore';
import * as logger from "firebase-functions/logger";
import * as admin from 'firebase-admin';

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Initialize the Admin SDK (required for server-side writes to Firestore)
admin.initializeApp();

// export const helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", { structuredData: true });
//   response.send("Hello from Firebase!");
// });

/**
 * Sync the daily streak leaderboard into:
 *   /leaderboards/dailyStreak/entries/{uid}
 *
 * This keeps the structure scalable for future leaderboards:
 *   /leaderboards/{leaderboardId}/entries/{uid}
 */
export const syncLeaderboardOnUserWrite = onDocumentWritten('users/{uid}', async (event) => {
  const uid = event.params?.uid ? String(event.params.uid) : undefined;
  if (!uid) {
    logger.error('Missing uid param in event', { event });
    return;
  }

  const leaderboardId = 'dailyStreak';
  const leaderboardEntryRef = admin.firestore()
    .collection('leaderboards')
    .doc(leaderboardId)
    .collection('entries')
    .doc(uid);

  try {
    const after = ((event.data as any)?.after?.data) as any || null;

    if (!after) {
      logger.log(`User ${uid} deleted — removing leaderboard entry`);
      await leaderboardEntryRef.delete();
      return;
    }

    const nickname = typeof after.nickname === 'string' ? after.nickname : '';
    const nicknameNormalized = nickname.trim().toLowerCase();
    const stats = after.stats || {};
    const score = typeof stats.currentStreakDaily === 'number' ? stats.currentStreakDaily : 0;

    await leaderboardEntryRef.set({
      uid,
      nickname,
      nicknameNormalized,
      score,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, { merge: true });

    logger.log(`Daily streak leaderboard updated for ${uid}`, { score });
  } catch (err) {
    logger.error('Error syncing leaderboard for user', uid, err);
    throw err;
  }
});
