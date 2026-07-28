# Java Generics

## Chapter 1 — Introduction

### 1.1 Generics

Generics add type parameters (e.g., `<T>`) to classes and interfaces so the compiler enforces type rules before runtime.

```java
// WITHOUT generics (raw type): every insert is an Object
List raw = new ArrayList();
raw.add("hello");
// Integer x = (Integer) raw.get(0);   // Compiles, but THROWS at runtime

// WITH generics: type-safe, no cast needed
List<String> words = new ArrayList<>();
words.add("hello");
String s = words.get(0);              // Compiler knows it's a String
```

### 1.2 Boxing and Unboxing

Primitive types cannot be type arguments. Autoboxing (`int` → `Integer`) and unboxing (`Integer` → `int`) bridge that gap automatically.

```java
List<Integer> nums = new ArrayList<>();
nums.add(42);         // autoboxed: Integer.valueOf(42)
nums.add(7);

// Foreach triggers unboxing
for (int n : nums) {  // unboxed from Integer to int
    System.out.println(n + 1);
}
```

### 1.3 Foreach

The enhanced for loop works over anything that implements `Iterable<E>` (or arrays). The loop variable is given the component type.

```java
// Iterable collection
List<String> fruits = Arrays.asList("apple", "banana");
for (String f : fruits) {
    System.out.println(f.toUpperCase());
}

// Array (covariant, reified)
int[] primes = {2, 3, 5, 7};
for (int p : primes) {
    System.out.print(p + " ");
}
```

### 1.4 Generic Methods and Varargs

A method can declare its own type parameters, independent of its class. Varargs with generic types create an implicit array and may trigger a heap-pollution warning; `@SafeVarargs` suppresses it when the method body is safe.

```java
// Generic method
public static <T> void fill(List<T> list, T val, int n) {
    for (int i = 0; i < n; i++) list.add(val);
}

// Generic varargs
@SafeVarargs
public final <T> List<T> gather(T... items) {
    return Arrays.asList(items);
}

// Usage
List<String> names = gather("A", "B", "C"); // T inferred as String
```

### 1.5 Assertions

The `assert` keyword (added in Java 1.5 alongside generics) lets you specify invariants. Because of erasure, you cannot assert a generic type at runtime, but you can assert sizes/nulls.

```java
public static <T> T first(List<T> list) {
    assert list != null        : "list must not be null";
    assert !list.isEmpty()     : "list must not be empty";
    return list.get(0);      // compiler inserts the cast after the check
}
// Run with -ea flag to enable assertions
```

---

## Chapter 2 — Subtyping and Wildcards

### 2.1 Subtyping and the Substitution Principle

The Substitution Principle says a variable of a base type may hold a subtype. But with generics: `List<Integer>` is NOT a subtype of `List<Object>` because generics are invariant.

```java
Integer i = 1;
Number n = i;                // OK: substitution for values

List<Integer> ints = new ArrayList<>();
// List<Number> nums = ints; // COMPILE ERROR: invariance!
// If allowed, we could add a Double to a List<Integer>.
```

### 2.2 Wildcards with extends

`? extends T` is covariance for reading. You can read `T`, but you cannot add anything (except `null`) because the actual list might be `List<S>` where `S` is a subtype of `T`.

```java
public static double sum(List<? extends Number> nums) {
    double total = 0;
    for (Number n : nums) {    // safe to read as Number
        total += n.doubleValue();
    }
    // nums.add(10);            // ERROR: could be List<Double>
    return total;
}

sum(List.of(1, 2, 3));         // OK: List<Integer>
sum(List.of(1.1, 2.2));        // OK: List<Double>
```

### 2.3 Wildcards with super

`? super T` is contravariance for writing. You can add `T` (or subtypes of `T`), but you can only read items as `Object`.

```java
public static void addIntegers(List<? super Integer> list) {
    list.add(10);              // OK: Integer fits any super list
    list.add(20);
    // Integer x = list.get(0); // ERROR: returns Object
    Object x = list.get(0);    // OK
}

addIntegers(new ArrayList<Number>());   // OK
addIntegers(new ArrayList<Object>());   // OK
```

### 2.4 The Get and Put Principle (PECS)

**Producer → extends (Get)**  
**Consumer → super (Put)**

```java
public static <T> void copy(List<? extends T> src,   // producer
                            List<? super T> dst) {   // consumer
    for (T item : src) {
        dst.add(item);
    }
}

List<Integer> ints = List.of(1, 2, 3);
List<Number> nums = new ArrayList<>();
copy(ints, nums); // T inferred as Integer
```

### 2.5 Arrays

Arrays are covariant and reified (they know their component type at runtime). Generics are invariant and erased. Mixing them is the primary reason generic array creation is forbidden.

```java
// Arrays: covariant but checked at runtime
Number[] nums = new Integer[10];
// nums[0] = 3.14;           // ArrayStoreException

// Generics: cannot create array of concrete parameterized type
// List<String>[] arr = new List<String>[10]; // COMPILE ERROR

// Workaround (unchecked, but common):
@SuppressWarnings("unchecked")
List<String>[] workaround = (List<String>[]) new List[10];
workaround[0] = List.of("hello");
```

### 2.6 Wildcards Versus Type Parameters

Use a type parameter (`<T>`) when the type appears in multiple places and must be the same. Use a wildcard (`?`) when you only care about a single occurrence and want maximum flexibility.

```java
// Type parameter: T links parameter to return type
public static <T> T merge(T a, T b) { return a; }

// Wildcard: more flexible for one-off consumption
public static void printAll(List<?> list) {
    for (Object o : list) System.out.println(o);
}
```

### 2.7 Wildcard Capture

When a method accepts `List<?>`, the compiler does not know what `?` is. A capture helper with a named type parameter solves this.

```java
public static void reverse(List<?> list) {
    reverseHelper(list);       // compiler captures ? as T
}

private static <T> void reverseHelper(List<T> list) {
    List<T> tmp = new ArrayList<>(list);
    list.clear();
    for (int i = tmp.size() - 1; i >= 0; i--) {
        list.add(tmp.get(i));  // now type-safe: T matches T
    }
}
```

### 2.8 Restrictions on Wildcards

- Cannot instantiate: `new ArrayList<?>()`
- Cannot use as the supertype of a class: `class MyList extends ArrayList<?>`
- Cannot call `add(T)` on `List<?>` (except `null`)

```java
List<?> mystery = new ArrayList<String>();
// mystery.add("x");        // ERROR
mystery.add(null);          // OK: null is member of every type
Object o = mystery.get(0);  // OK
```

---

## Chapter 3 — Comparison and Bounds

### 3.1 Comparable

`Comparable<T>` defines natural ordering via `compareTo(T)`. A class compares itself to another instance of a related type.

```java
class Student implements Comparable<Student> {
    int score;
    Student(int s) { this.score = s; }
    public int compareTo(Student other) {
        return Integer.compare(this.score, other.score);
    }
}
```

### 3.2 Maximum of a Collection

To find a maximum, you need a type bounded by `Comparable`. The best practice is `Comparable<? super T>` so that a subtype can reuse a base-class comparison.

```java
public static <T extends Comparable<? super T>> T max(Collection<? extends T> coll) {
    T best = null;
    for (T t : coll) {
        if (best == null || t.compareTo(best) > 0) {
            best = t;
        }
    }
    return best;
}

List<String> words = Arrays.asList("cat", "elephant", "ant");
String longest = max(words); // "elephant" (if compareTo is natural String order)
```

### 3.3 A Fruity Example

If `Apple` extends `Fruit`, and `Apple` implements `Comparable<Apple>`, it is not `Comparable<Fruit>`. Using `Comparable<? super T>` lets you call `max` on a collection of apples because `Comparable<Apple>` matches `Comparable<? super Apple>`.

```java
class Fruit implements Comparable<Fruit> {
    String name;
    public int compareTo(Fruit f) { return name.compareTo(f.name); }
}
class Apple extends Fruit { /* inherits Comparable<Fruit> */ }

List<Apple> apples = Arrays.asList(new Apple(), new Apple());
Apple a = max(apples); // works because bound is Comparable<? super Apple>
```

### 3.4 Comparator

`Comparator<T>` provides external ordering when natural ordering is insufficient.

```java
Comparator<String> byLength = (a, b) -> Integer.compare(a.length(), b.length());
List<String> words = new ArrayList<>(List.of("cat", "elephant", "ant"));
words.sort(byLength); // [ant, cat, elephant]
```

### 3.5 Enumerated Types

An enum is compiled as a generic class extending `Enum<E>` where `E` is the enum itself (the self-type idiom).

```java
enum Season { WINTER, SPRING, SUMMER, FALL }
// Compiled as: class Season extends Enum<Season>
Season s = Enum.valueOf(Season.class, "WINTER");
```

### 3.6 Multiple Bounds

A type parameter may have one class bound and multiple interface bounds: `<T extends Number & Comparable<T> & Serializable>`.

```java
class Stat<T extends Number & Comparable<T> & java.io.Serializable> {
    private final T value;
    Stat(T v) { this.value = v; }
    int compare(Stat<T> other) {
        return this.value.compareTo(other.value); // OK: T is Comparable
    }
}
```

### 3.7 Bridges

When a class implements a parameterized interface, the compiler generates a bridge method to route the erased call (`compareTo(Object)`) to the typed method (`compareTo(String)`).

```java
class MyComp implements Comparable<String> {
    public int compareTo(String s) { return s.length(); }
    // Compiler synthesizes:
    // public int compareTo(Object o) { return compareTo((String) o); }
}

Comparable c = new MyComp();
c.compareTo("hi");   // calls bridge → compareTo(String)
// c.compareTo(42);  // ClassCastException inside bridge cast
```

### 3.8 Covariant Overriding

Java allows an overriding method to change its return type to a subtype (covariant return). This also works for generic returns.

```java
class Node {
    public Number value() { return 1; }
}
class IntNode extends Node {
    @Override
    public Integer value() { return 42; } // covariant return: Integer <: Number
}

// With generics
abstract class Box<T> { abstract T get(); }
class StrBox extends Box<String> {
    @Override String get() { return "hello"; }
}
```

---

## Chapter 4 — Declarations

### 4.1 Constructors

A constructor cannot declare its own type parameters, but it may use the type variables declared by the enclosing class in its signature and body.

```java
class Pair<K, V> {
    K key; V value;

    // Uses K and V from the class declaration
    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
    // ILLEGAL: <T> Pair(T t) { ... }  ← constructors cannot have <T>
}

Pair<String, Integer> p = new Pair<>("age", 30);
```

### 4.2 Static Members

Static fields and methods cannot refer to the enclosing class’s type variables because they belong to the class, not an instance. However, a static method can declare new type parameters of its own.

```java
class Wrapper<T> {
    T instanceField;                 // OK

    // static T staticField;         // ERROR: no access to T in static context

    // But static methods can introduce fresh type parameters
    static <S> S identity(S x) { return x; }
    static <U> void swap(U[] a, int i, int j) {
        U tmp = a[i]; a[i] = a[j]; a[j] = tmp;
    }
}
```

### 4.3 Nested Classes

- Inner classes (non-static) have access to the outer class’s type parameters.
- Static nested classes do not; they must declare their own.
- Anonymous classes can capture method-level type variables.

```java
class Outer<T> {
    T data;

    // Inner sees T
    class Inner {
        T get() { return data; }
    }

    // Static nested needs its own parameter
    static class Holder<S> {
        S value;
    }

    // Anonymous class in generic method
    <U> Comparator<U> compareByHash() {
        return new Comparator<U>() {
            public int compare(U a, U b) {
                return Integer.compare(a.hashCode(), b.hashCode());
            }
        };
    }
}
```

### 4.4 How Erasure Works

The compiler erases type parameters:

1. Replaces every type variable with its first bound (or `Object` if unbounded).
2. Inserts casts at call sites.
3. Generates bridge methods to maintain polymorphism.

```java
// SOURCE
public class Node<T> {
    private T data;
    public Node(T data) { this.data = data; }
    public void setData(T data) { this.data = data; }
    public T getData() { return data; }
}

// EFFECTIVE BYTECODE AFTER ERASURE
public class Node {
    private Object data;
    public Node(Object data) { this.data = data; }
    public void setData(Object data) { this.data = data; }
    public Object getData() { return data; }
}

// CALL SITE
Node<String> n = new Node<>("hi");
String s = n.getData();  // compiler silently inserts: (String) n.getData();
```

---

## Chapter 5 — Evolution, Not Revolution

### 5.1 Raw Types and Legacy Interoperability

A raw type is a generic class used without arguments. It exists solely for backward compatibility with pre-Java 5 code.

```java
// Legacy library returns raw List
List raw = new ArrayList();     // raw type
raw.add("hello");

// Generic client using legacy code
List<String> unsafe = raw;      // unchecked warning
// String s = unsafe.get(0);    // compiles, may throw ClassCastException
```

### 5.2 Unchecked Warnings

Mixing raw types with generics produces unchecked warnings for assignment, cast, and method call. The compiler cannot guarantee type safety.

```java
@SuppressWarnings("unchecked")  // only when you can prove safety
public static <T> List<T> toList(T... elems) {
    List<T> list = (List<T>) new ArrayList(); // raw creation + cast
    Collections.addAll(list, elems);
    return list;
}
```

### 5.3 Bridge Methods in Evolution

Because legacy VMs know nothing of generics, the compiler emits bridge methods so that a class implementing `Comparable<String>` still answers to the erased `compareTo(Object)` that legacy clients might invoke.

```java
class MyString implements Comparable<MyString> {
    public int compareTo(MyString s) { return 0; }
}
// javap reveals two methods:
// public int compareTo(MyString)  ← user-written
// public int compareTo(Object)     ← synthetic bridge calling the above
```

### 5.4 Casts, instanceof, and Erasure

You cannot query a generic type at runtime. There is no `List<String>.class` — only `List.class`.

```java
List<String> list = new ArrayList<>();
// if (list instanceof List<String>) {}  // SYNTAX ERROR

// You can only test the raw type
if (list instanceof List<?>) { /* yes */ }

// Casting to a parameterized type is unchecked
Object obj = new ArrayList<String>();
@SuppressWarnings("unchecked")
List<String> safe = (List<String>) obj;
```

### 5.5 Arrays and Generics (Reification vs Erasure)

Arrays are reified (JVM tracks `Integer[]`); generics are erased (JVM sees `ArrayList`). You cannot create a generic array directly because the JVM could not enforce store checks.

```java
// List<String>[] array = new List<String>[10]; // COMPILE ERROR

// Workaround with raw array + cast
@SuppressWarnings("unchecked")
List<String>[] force = (List<String>[]) new List[10];

// Better: avoid arrays entirely
List<List<String>> matrix = new ArrayList<>();
```

### 5.6 Transition Strategy: Generic Library / Generic Client

Modern code uses full generics internally, but exposes raw-compatible signatures during transition. The binary output (`.class`) remains compatible because the erased signature stays identical.

```java
// Library after genericization
public static final <T> List<T> emptyList() { ... }

// Legacy client compiled against raw List
List oldStyle = Collections.emptyList();  // OK via raw type
```

### 5.7 Limitations of Erasure

Because generics were bolted onto an existing VM without changing its instruction set, Java pays these permanent costs:

- Cannot instantiate `new T()`
- Cannot create `new T[10]`
- Cannot use `instanceof T`
- Cannot use primitive type arguments directly (need wrappers)
- Cannot have static `T` fields
- No generic `.class` literals

```java
public final class ErasureCosts<T> {
    // T t = new T();          // ERROR: no runtime type token
    // if (x instanceof T) {}  // ERROR
    // static T val;           // ERROR
    Class<?> c = ErasureCosts.class; // only raw class exists
}
```

---

## Quick Reference Cheat-Sheet

| Concept | Syntax Rule |
|---|---|
| Invariance | `List<Integer>` ≮: `List<Number>` |
| Covariance (read) | `? extends T` |
| Contravariance (write) | `? super T` |
| Maximum flexibility | `?` |
| Multiple bounds | `<T extends Number & Comparable>` |
| Comparable bound | always `Comparable<? super T>` |
| Static access | cannot use enclosing class `<T>`, can declare own `<S>` |
| Constructor | uses class `<T>`, cannot declare its own |
| Wildcard capture | anonymous `?` → named via private `<T>` helper |
| Legacy interop | raw types compile, but generate unchecked warnings |
| Erasure | generics vanish; casts & bridges are injected |
