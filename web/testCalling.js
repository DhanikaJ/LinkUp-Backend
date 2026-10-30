/**
 * LinkUp Calling Feature - Test Script
 * 
 * This script tests the WebSocket signaling for calls
 * Run with: node testCalling.js
 */

const WebSocket = require('ws');

const WS_URL = ' https://68f42c47e6e2.ngrok-free.app/LinkUp/chat'; // Update with your URL
const USER1_ID = 14; // Dhanika
const USER2_ID = 15; // Anderson

let user1Socket;
let user2Socket;

console.log('🧪 Starting LinkUp Calling Tests...\n');

// Test 1: Connect both users
function test1_connect() {
  return new Promise((resolve) => {
    console.log('📡 Test 1: Connecting users...');
    
    user1Socket = new WebSocket(`${WS_URL}?userId=${USER1_ID}`);
    user2Socket = new WebSocket(`${WS_URL}?userId=${USER2_ID}`);
    
    let connected = 0;
    
    user1Socket.on('open', () => {
      console.log(`✅ User ${USER1_ID} connected`);
      connected++;
      if (connected === 2) resolve();
    });
    
    user2Socket.on('open', () => {
      console.log(`✅ User ${USER2_ID} connected`);
      connected++;
      if (connected === 2) resolve();
    });
    
    user1Socket.on('error', (err) => console.error('❌ User1 error:', err));
    user2Socket.on('error', (err) => console.error('❌ User2 error:', err));
  });
}

// Test 2: Initiate call from User1 to User2
function test2_initiateCall() {
  return new Promise((resolve) => {
    console.log('\n📞 Test 2: Initiating call...');
    
    // User2 should receive incoming_call
    user2Socket.once('message', (data) => {
      const msg = JSON.parse(data);
      if (msg.type === 'incoming_call') {
        console.log('✅ User2 received incoming call');
        console.log(`   Caller: ${msg.payload.callerName}`);
        console.log(`   Type: ${msg.payload.callType}`);
        resolve();
      }
    });
    
    // User1 initiates call
    user1Socket.send(JSON.stringify({
      type: 'call_initiate',
      calleeId: USER2_ID,
      callType: 'audio',
      userId: USER1_ID
    }));
    
    console.log(`📤 User ${USER1_ID} initiated call to ${USER2_ID}`);
  });
}

// Test 3: User2 accepts call
function test3_acceptCall() {
  return new Promise((resolve) => {
    console.log('\n✅ Test 3: Accepting call...');
    
    // User1 should receive call_accepted
    user1Socket.once('message', (data) => {
      const msg = JSON.parse(data);
      if (msg.type === 'call_accepted') {
        console.log('✅ User1 received call accepted');
        console.log(`   Callee ID: ${msg.payload.calleeId}`);
        resolve();
      }
    });
    
    // User2 accepts call
    user2Socket.send(JSON.stringify({
      type: 'call_accept',
      callerId: USER1_ID,
      userId: USER2_ID
    }));
    
    console.log(`📤 User ${USER2_ID} accepted call`);
  });
}

// Test 4: WebRTC Signaling - Offer
function test4_webrtcOffer() {
  return new Promise((resolve) => {
    console.log('\n🔄 Test 4: WebRTC Offer exchange...');
    
    // User2 should receive offer
    user2Socket.once('message', (data) => {
      const msg = JSON.parse(data);
      if (msg.type === 'webrtc_offer') {
        console.log('✅ User2 received WebRTC offer');
        console.log(`   From: ${msg.payload.fromUserId}`);
        resolve();
      }
    });
    
    // User1 sends offer
    const mockOffer = {
      type: 'offer',
      sdp: 'v=0\no=- 123456 2 IN IP4 127.0.0.1...'
    };
    
    user1Socket.send(JSON.stringify({
      type: 'webrtc_offer',
      targetUserId: USER2_ID,
      offer: JSON.stringify(mockOffer),
      userId: USER1_ID
    }));
    
    console.log('📤 User1 sent WebRTC offer');
  });
}

// Test 5: WebRTC Signaling - Answer
function test5_webrtcAnswer() {
  return new Promise((resolve) => {
    console.log('\n🔄 Test 5: WebRTC Answer exchange...');
    
    // User1 should receive answer
    user1Socket.once('message', (data) => {
      const msg = JSON.parse(data);
      if (msg.type === 'webrtc_answer') {
        console.log('✅ User1 received WebRTC answer');
        console.log(`   From: ${msg.payload.fromUserId}`);
        resolve();
      }
    });
    
    // User2 sends answer
    const mockAnswer = {
      type: 'answer',
      sdp: 'v=0\no=- 789012 2 IN IP4 127.0.0.1...'
    };
    
    user2Socket.send(JSON.stringify({
      type: 'webrtc_answer',
      targetUserId: USER1_ID,
      answer: JSON.stringify(mockAnswer),
      userId: USER2_ID
    }));
    
    console.log('📤 User2 sent WebRTC answer');
  });
}

// Test 6: ICE Candidate exchange
function test6_iceCandidate() {
  return new Promise((resolve) => {
    console.log('\n🧊 Test 6: ICE Candidate exchange...');
    
    // User2 should receive ICE candidate
    user2Socket.once('message', (data) => {
      const msg = JSON.parse(data);
      if (msg.type === 'webrtc_ice_candidate') {
        console.log('✅ User2 received ICE candidate');
        resolve();
      }
    });
    
    // User1 sends ICE candidate
    const mockCandidate = {
      candidate: 'candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host',
      sdpMLineIndex: 0,
      sdpMid: 'audio'
    };
    
    user1Socket.send(JSON.stringify({
      type: 'webrtc_ice_candidate',
      targetUserId: USER2_ID,
      candidate: JSON.stringify(mockCandidate),
      userId: USER1_ID
    }));
    
    console.log('📤 User1 sent ICE candidate');
  });
}

// Test 7: End call
function test7_endCall() {
  return new Promise((resolve) => {
    console.log('\n📴 Test 7: Ending call...');
    
    // User2 should receive call_ended
    user2Socket.once('message', (data) => {
      const msg = JSON.parse(data);
      if (msg.type === 'call_ended') {
        console.log('✅ User2 received call ended');
        console.log(`   Ended by: ${msg.payload.endedBy}`);
        resolve();
      }
    });
    
    // User1 ends call
    user1Socket.send(JSON.stringify({
      type: 'call_end',
      otherUserId: USER2_ID,
      userId: USER1_ID
    }));
    
    console.log(`📤 User ${USER1_ID} ended call`);
  });
}

// Test 8: Reject call scenario
function test8_rejectCall() {
  return new Promise((resolve) => {
    console.log('\n❌ Test 8: Reject call scenario...');
    
    // Setup: User1 initiates call
    user2Socket.once('message', (data) => {
      const msg = JSON.parse(data);
      if (msg.type === 'incoming_call') {
        console.log('📥 User2 received incoming call');
        
        // User1 should receive rejection
        user1Socket.once('message', (data) => {
          const msg = JSON.parse(data);
          if (msg.type === 'call_rejected') {
            console.log('✅ User1 received call rejected');
            resolve();
          }
        });
        
        // User2 rejects
        user2Socket.send(JSON.stringify({
          type: 'call_reject',
          callerId: USER1_ID,
          userId: USER2_ID
        }));
        
        console.log('📤 User2 rejected call');
      }
    });
    
    // Initiate new call
    user1Socket.send(JSON.stringify({
      type: 'call_initiate',
      calleeId: USER2_ID,
      callType: 'video',
      userId: USER1_ID
    }));
  });
}

// Run all tests
async function runTests() {
  try {
    await test1_connect();
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await test2_initiateCall();
    await new Promise(resolve => setTimeout(resolve, 500));
    
    await test3_acceptCall();
    await new Promise(resolve => setTimeout(resolve, 500));
    
    await test4_webrtcOffer();
    await new Promise(resolve => setTimeout(resolve, 500));
    
    await test5_webrtcAnswer();
    await new Promise(resolve => setTimeout(resolve, 500));
    
    await test6_iceCandidate();
    await new Promise(resolve => setTimeout(resolve, 500));
    
    await test7_endCall();
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await test8_rejectCall();
    await new Promise(resolve => setTimeout(resolve, 500));
    
    console.log('\n\n🎉 All tests passed! ✅');
    console.log('═══════════════════════════════════════');
    console.log('✅ WebSocket connections working');
    console.log('✅ Call initiation working');
    console.log('✅ Call acceptance working');
    console.log('✅ Call rejection working');
    console.log('✅ Call ending working');
    console.log('✅ WebRTC signaling working');
    console.log('✅ ICE candidate exchange working');
    console.log('═══════════════════════════════════════');
    console.log('\n🚀 Your calling feature is ready!');
    
    // Close connections
    user1Socket.close();
    user2Socket.close();
    
    process.exit(0);
    
  } catch (error) {
    console.error('\n❌ Test failed:', error);
    user1Socket?.close();
    user2Socket?.close();
    process.exit(1);
  }
}

// Handle errors
process.on('unhandledRejection', (error) => {
  console.error('❌ Unhandled error:', error);
  process.exit(1);
});

// Start tests
console.log('═══════════════════════════════════════');
console.log('  LinkUp Calling Feature Test Suite');
console.log('═══════════════════════════════════════');
console.log(`Backend URL: ${WS_URL}`);
console.log(`User 1 ID: ${USER1_ID}`);
console.log(`User 2 ID: ${USER2_ID}`);
console.log('═══════════════════════════════════════\n');

runTests();