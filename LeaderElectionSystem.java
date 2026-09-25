import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates a ring-based leader election algorithm (Chang-Roberts style)
 * among N concurrent participants arranged in a logical ring.
 *
 * Each participant runs on its own thread. A participant that starts an
 * election forwards its own id around the ring; any participant that
 * receives an id greater than its own simply relays it onward, while a
 * participant that receives its own id back knows it has been elected
 * leader.
 */
public class LeaderElectionSystem {

    class Participant implements Runnable {
        final int participantId;
        final AtomicBoolean isActive;
        Participant[] allParticipants;

        Participant(int participantId, Participant[] allParticipants) {
            this.participantId = participantId;
            this.isActive = new AtomicBoolean(true);
            this.allParticipants = allParticipants;
        }

        @Override
        public void run() {
            initiateElection();
        }

        private void initiateElection() {
            Participant nextParticipant = nextParticipant();
            System.out.println("Participant " + participantId +
                    " sends an election message to Participant " + nextParticipant.participantId);
            nextParticipant.receiveElectionMessage(participantId);
        }

        void receiveElectionMessage(int senderId) {
            if (!isActive.get()) {
                return;
            }
            if (senderId > participantId) {
                Participant nextParticipant = nextParticipant();
                System.out.println("Participant " + participantId +
                        " forwards leadership candidate " + senderId +
                        " to Participant " + nextParticipant.participantId);
                nextParticipant.receiveElectionMessage(senderId);
            } else if (senderId == participantId) {
                isActive.set(false);
                System.out.println("Participant " + participantId + " is declared as the leader.");
            }
            // senderId < participantId and this participant is still active:
            // the message is dropped, since this participant's own (higher) id
            // will eventually make it around the ring instead.
        }

        private Participant nextParticipant() {
            int nextIndex = participantId % allParticipants.length; // 0-based index of the next participant
            return allParticipants[nextIndex];
        }
    }

    private int numParticipants;
    private Participant[] participants;

    public void initializeLeaderElectionSystem(int numParticipants) {
        System.out.println("Number of participants: " + numParticipants);
        this.numParticipants = numParticipants;
        participants = new Participant[numParticipants];
        for (int i = 0; i < numParticipants; i++) {
            participants[i] = new Participant(i + 1, participants); // ids start from 1
        }
    }

    public void startElection() {
        Thread[] participantThreads = new Thread[numParticipants];
        for (int i = 0; i < numParticipants; i++) {
            participantThreads[i] = new Thread(participants[i], "participant-" + (i + 1));
            participantThreads[i].start();
        }

        for (Thread thread : participantThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        int numParticipants = args.length > 0 ? Integer.parseInt(args[0]) : 5;

        LeaderElectionSystem electionSystem = new LeaderElectionSystem();
        electionSystem.initializeLeaderElectionSystem(numParticipants);
        electionSystem.startElection();
    }
}
