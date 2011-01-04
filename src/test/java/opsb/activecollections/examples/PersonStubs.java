package opsb.activecollections.examples;

public class PersonStubs {

	public static Person paul() {
		return new Person("Paul");
	}
	
	public static Person peter() {
		return new Person("Peter", new Address("The Strand", 59));
	}
	
	public static Person mark() {
		return new Person("Mark");
	}
	
	public static Person jim() {
		return new Person("Jim");
	}
	
	public static Person jim_carrey() {
		return new Person("Jim", "carrey");
	}
	
	public static Person jim_cramer() {
		return new Person("Jim", "cramer");
	}
	
	public static Person james() {
		return new Person("James");
	}
	
	public static Person gabrial() {
		return new Person("Gabrial");
	}
	
	public static Person pearson() {
		return new Person("Pearson");
	}
	
	public static Person zack() {
		return new Person("Zack");
	}
	
}
