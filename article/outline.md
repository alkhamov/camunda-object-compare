# Beyond Decision Tables: Using DMN to Make System Logic Configurable

## 1. Introduction

DMN is commonly associated with business rules and decision tables. However,
a DMN engine can also perform more sophisticated decision logic, particularly
when combined with FEEL expressions.

This can be useful when some system behavior needs to remain configurable
without requiring application code changes and the associated build, test,
and deployment cycle.

One example is comparing selected attributes of complex nested objects.

This article demonstrates this idea with a small proof of concept using:
- Camunda 7
- DMN
- FEEL
- BPMN
- Java external workers
- MongoDB

The objective is not to turn DMN into a general-purpose programming language.
Instead, the example explores where moving suitable decision logic into DMN
can reduce coupling between application code and configurable system behavior.


## 2. The Example: Comparing Nested Objects

Assume two objects contain nested service characteristics:

{
  "serviceCharacteristic": [
    {
      "name": "characteristicA",
      "value": {
        "value": "valueA"
      }
    },
    {
      "name": "characteristicB",
      "value": {
        "value": "valueB"
      }
    }
  ]
}

The application needs to determine whether relevant characteristics of
Object A and Object B have equal values.

A conventional implementation could perform this navigation and comparison
inside Java code.

The PoC takes a different approach:

- BPMN orchestrates the process.
- Java workers retrieve and transport complete objects.
- DMN owns the comparison logic.
- FEEL navigates the nested object structure and performs the comparison.


## 3. Part 1 — Comparison Logic Inside DMN

### 3.1 V1 Decision Model

[DRD diagram for V1]

Object A ─────┐
              ├──> Object Comparison ──> comparisonResult
Object B ─────┘

The worker passes the complete objects to the decision.

It does not extract `characteristicA`, `characteristicB`, etc. before calling
the DMN decision.


### 3.2 FEEL Comparison

Show the V1 FEEL expression:

if objectA = null or objectB = null then false
else every a in objectA.serviceCharacteristic satisfies
  some b in objectB.serviceCharacteristic satisfies
    a.name = b.name and a.value.value = b.value.value

Explain briefly:

- `every` iterates over the characteristics from Object A.
- `some` finds a characteristic with the same name in Object B.
- FEEL navigates the nested `value.value` structure.
- The result is a Boolean.
- No characteristic-specific comparison logic exists in Java.

Mention the important V1 semantic:
Object A implicitly defines the set of characteristics that must match.


### 3.3 Data Mapping

Explain only what is necessary.

MongoDB returns BSON documents. The worker converts the complete BSON document
into a generic structure suitable for a Camunda process variable.

This is technical serialization/mapping, not business mapping.

The worker does NOT perform logic such as:

"Find characteristicA and compare its value."

That responsibility remains inside DMN.


## 4. Part 2 — Making the Comparison Set Configurable

V1 moves the comparison algorithm into DMN, but Object A still implicitly
defines what must be compared.

The next step is to make the comparison set itself explicit.


### 4.1 V2 Decision Model

[DRD diagram for V2]

           Comparison Configuration
                    │
                    ▼
Object A ───────> Object Comparison V2 <────── Object B
                    │
                    ▼
             comparisonResult

`Comparison Configuration` can return, for example:

["characteristicA", "characteristicC"]

The comparison decision then evaluates only those characteristics.


### 4.2 Why This Matters

Suppose:

                     Object A       Object B
characteristicA      valueA         valueA
characteristicB      valueB         DIFFERENT
characteristicC      valueC         valueC

With configuration:

["characteristicA", "characteristicC"]

the result is:

true

The difference in `characteristicB` is irrelevant because it is not part of
the configured comparison set.

Changing which characteristics participate in the decision therefore does not
require changing the Java worker or BPMN orchestration.


## 5. Where This Could Go Further

The PoC uses a simple list of characteristic names, but the same idea could be
extended.

For example, configuration could eventually describe:

- which characteristics participate in comparison;
- different comparison rules for different characteristics;
- optional versus mandatory characteristics;
- normalization rules;
- conditional comparison depending on object type or context.

At that point, DMN becomes more than a single comparison expression: it
becomes a configurable decision layer around the comparison behavior.

Care is still required not to move arbitrary application logic into DMN.
The logic should remain suitable for expression as decisions and rules.


## 6. Conclusion

DMN is often first encountered through relatively simple decision tables, but
its capabilities are not limited to them.

Combined with FEEL, DMN can navigate structured input data, evaluate nested
values, iterate over collections, and compose multiple decisions.

For suitable use cases, this makes it possible to move selected system logic
from application code into configurable decision models.

The object-comparison PoC demonstrates this progression:

V1:
comparison algorithm belongs to DMN.

V2:
both the comparison algorithm and the comparison configuration belong to the
decision model.

The application remains responsible for retrieving and transporting data,
while DMN determines how that data should be interpreted for the decision.

This separation can reduce the need for application code changes when decision
behavior changes and can make selected system behavior easier to configure
independently.