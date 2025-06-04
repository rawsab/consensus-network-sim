package cmg.cnsim.testblockchain.node;

public enum NodeState {
    // Initiated Group
    WAITING,
    INITIATED,
    WAITING_VALIDATE_SUBMISSIONS,
    FAULT_SUSPICION_INITIATED,

    // ValidateSubmissions Group
    VALIDATE_SUBMISSIONS,
    WAITING_VALIDATE_HASH,
    FAULT_SUSPICION_VALIDATE_SUBMISSIONS,

    // ValidateHash Group
    VALIDATE_HASH,
    COMPLETED,
    FAULT_SUSPICION_VALIDATE_HASH,

    // Fault
    NODE_FAULT,
    NETWORK_FAULT;


}