const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validAlarmPayload(userId = ALICE_UID, overrides = {}) {
  const now = new Date();
  return {
    userId,
    title: "Cell Culture Media Exchange",
    notes: "Check 37.0°C CO2 incubator & passage flask #2",
    hour: 9,
    minute: 30,
    enabled: true,
    category: "CELL_CULTURE",
    daysOfWeek: [1, 3, 5],
    bioTempCelsius: "37.0°C",
    protocolDurationMin: 45,
    vibrate: true,
    snoozeMinutes: 10,
    completedCount: 0,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or list work_alarms", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("work_alarms").get());
  await assertFails(unauthDb.collection("work_alarms").doc("alarm_1").get());
});

test("Authenticated user: can create valid work alarm for themselves", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("work_alarms").doc("alarm_1").set(validAlarmPayload(ALICE_UID))
  );
});

test("Authenticated user: cannot spoof userId on create", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("work_alarms").doc("alarm_spoof").set(validAlarmPayload(BOB_UID))
  );
});

test("Authenticated user: cannot inject ghost fields on create or update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("work_alarms").doc("alarm_ghost").set(
      validAlarmPayload(ALICE_UID, { isVerified: true })
    )
  );
});

test("Authenticated user: cannot read or update another user's work alarm", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("work_alarms").doc("bob_alarm").set(validAlarmPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("work_alarms").doc("bob_alarm").get());
  await assertFails(
    aliceDb.collection("work_alarms").doc("bob_alarm").update({ enabled: false, updatedAt: new Date() })
  );
});

test("Authenticated user: can query their own alarms with matching userId filter", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("work_alarms").doc("alice_alarm").set(validAlarmPayload(ALICE_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("work_alarms").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: fails query without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("work_alarms").get());
});
