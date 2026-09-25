# Internal Contracts

This project has no public external API for end users. The key contracts are internal to the BPMN and DMN runtime and are defined by process variables and decision input/output payloads.

For the concrete comparison contract, see:

- `dmn-comparison-contract.md`

The core rule is that BPMN only orchestrates values and the DMN decision alone owns the comparison semantics.
