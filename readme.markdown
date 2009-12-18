Install
-------
Just update your maven settings with

    <repository>
        <id>opsbreleases</id>
        <name>opsb-releases</name>
        <url>http://opsb.co.uk/nexus/content/repositories/releases/</url>
    </repository>
and

    <dependency>
        <groupId>com.googlecode</groupId>
        <artifactId>active-collections</artifactId>
        <version>0.18-SNAPSHOT</version>
    </dependency>

Usage
-----
Got an Article object with JPA mappings? Let's create an active set for it.

    public class Articles extends JpaActiveSet<Article> {
  
        public Articles() {} // Required for cglib - transactions etc.
  
        public Articles(EntityManagerFactory emf) {
            super(Article.class, emf);
        }
  
    }


### Create the Set
    Articles articles = new Articles(entityManagerFactory);

### Saving articles
    Article article = new Article("US drones hacked by Iraqi insurgents");
    articles.add(article);

### Count
    1 == articles.size();
    false == articles.isEmpty();
    
### Exists?
    true == articles.contains(article)
    
### Deleting articles
    articles.remove(article);

### For loops

    for(Article article : articles) {
      System.out.println(article.getName());
    }

### Used in place of a Set
    Set<Article> setOfArticles = articles;
All methods of the Set interface have been implemented so your Articles can go anywhere a normal Set can.

### Find
    Article foundUsingId = articles.find(article.getId())
    articles.find(idForArticleThatDoesntExist) // throws IllegalArgumentException
    null == articles.findOrNull(idForArticleThatDoesntExist)

### Paging
    Articles firstPage = articles.page(1); // default page size is 25, indexed from 1
    Articles smallerPage = articles.pagesOf(10).page(1);

### Sorting
    Articles sortedByName = articles.orderedBy("name DESC");

### Chaining
Notice that many of these methods return an object of type Articles. This allows us to chain them together.

    Articles orderedByNamePageOne = articles.orderedBy("name DESC").pagesOf(20).page(1)

### Filtering
active-collections allows you to define custom filtering criteria, let's extend our Article implementation.

    public class Articles extends JpaActiveSet<Article> {

        public Articles() {} // Required for cglib - transactions etc.

        public Articles(EntityManagerFactory emf) {
            super(Article.class, emf);
        }
    
        public Articles beginningWith(String startOfName) {
            return where("article.name like ?", startOfName + "%");
        }
    
        public Articles endingWith(String endOfName) {
            return where("article.name like ?", "%" + endOfName);
        }

    }

now we can do

Articles articlesWithNamesBeginningWithPEndingWithE = articles.beginningWith("P").endingWith("e");

Note that you can add as many conditions as you like and then just chain them up. Why not try it in a for loop

for(Article article : articles.beginningWith("P").endingWith("e")) {
  ...
}

