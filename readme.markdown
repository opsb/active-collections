Install
-------
Update your maven settings with

    <repository>
        <id>opsbreleases</id>
        <name>opsb-releases</name>
        <url>http://opsb.co.uk/nexus/content/repositories/releases/</url>
    </repository>
    <repository>
        <id>opsbsnapshots</id>
        <name>opsb-snapshots</name>
        <url>http://opsb.co.uk/nexus/content/repositories/snapshots/</url>
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


### Hook up to the db
    Articles articles = new Articles(entityManagerFactory);

### Saving articles
    Article article = new Article("US drones hacked by Iraqi insurgents");
    articles.add(article);

### Count
    1 == articles.count();
    1 == articles.size(); // Takes paging into account to conform to Set contract i.e. pagesize determines maximum possible result from this method. Normally you'll want to use count().
    false == articles.isEmpty();
    
### Exists?
    true == articles.contains(article);
    false == articles.containsAll(collectionOfArticles);
    
### Deleting articles
    articles.remove(article);
    articles.removeAll(collectionOfArticles);

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
    Articles smallerPageSize = articles.pagesOf(10).page(1);

### Sorting
    Articles sortedByName = articles.orderedBy("name DESC");

### Chaining
Notice that many of these methods return an object of type Articles. This allows us to chain them together.

    Articles orderedByNamePageOne = articles.orderedBy("name DESC").pagesOf(20).page(1)

### Filtering
active-collections allows you to define custom filtering criteria, let's extend our Article implementation. We're going to take advantage of the where(jpaFragment, param1, param2, ...) method. JpaActiveSet makes an alias available for you so that you can just refer to the entity easily. The convention is that the alias is the lowercase name of the entity, in this example "article".

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
        
        public Articles publishedBetween(Date startDate, Date endDate) {
            return where("article.publishedDate between ? and ?", startDate, endDate);
        }

    }

now we can do

    Articles articlesWithNamesBeginningWithPEndingWithE = articles.beginningWith("P").endingWith("e");
    Articles q3Articles = articles.publishedBetween(JULY,OCTOBER);

Note that you can add as many conditions as you like and then just chain them up. Why not try it in a for loop

    for(Article article : articles.beginningWith("P").endingWith("e")) {
      ...
    }

#### Dates and Calendars as parameters
They just work. You don't have to worry about telling JPA that they are time based parameters, JpaActiveSet takes care of it for you.

#### Collections as parameters using ?
They also just work. JPA will not normally allow you to use Collections as parameters when you're using the ? syntax. It does however work with named parameters. Behind the scenes JpaActiveSet actually converts all ?s into named parameters so you're able to use Collections as parameters with the ? syntax.


### Logging
The logging framework is log4j. By setting the logger level for com.googlecode.activecollections.JpaActiveSet you can view the jpa queries as they're executed.

Sometimes you only want logging from one of your JpaActiveSets. Taking Articles as an example

    import org.apache.log4j.Logger;
    public Articles extends JpaActiveSet<Article> {
  
        //...
  
        @Override
        protected Logger getLogger() {
            return Logger.getLogger(Articles.class);
        }
	
    }

now when you switch on logging for Articles you'll see all of the JPA queries that JpaActiveSet is executing.

### Freezing
By default JpaActiveSets are always evaluated against the db _every_ time you use a method. You might want to freeze the results, in which case these are for you.

    Set<Article> frozenSet = articles.frozen();
    List<Article> frozenList = articles.frozenList();
    SortedSet<Article> frozenSortedSet = articles.frozenSortedSet();
    Set<Article> orderedSet = articles.frozenOrderedSet();
  
### Testing    
#### Mocking JpaActiveSets for testing
Mockito is your friend here, it allows you to do "deep stubbing". This means you can define expectations for chains in one go.

    Articles mockArticles = deepMock(Articles.class);
    when(mockArticles.publishedBetween(startDate,endDate).beginningWith("P").frozen())
      .thenReturn(asSet(article1, article2));

and this is the implementation for the deepMock method

    public class MockitoUtil {
        public static <T> T deepMock(Class<T> clazz) {
                return Mockito.mock(clazz, new DeepAnswer());
        }
    }

    class DeepAnswer implements Answer<Object> {
        private static final long serialVersionUID = -6926328908792880098L;

        private final HashMap<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();

        public Object answer(InvocationOnMock invocation) throws Throwable {
            Class<?> clz = invocation.getMethod().getReturnType();
            if (clz.isPrimitive()) {
                    return null;
            }
            if (mocks.containsKey(clz)) {
                    return mocks.get(clz);
            } else {
                    Object mock = Mockito.mock(clz, this);
                    mocks.put(clz, mock);
                    return mock;
            }
        }
    }  
  
### Gotchas
#### Dependencies missing after filtering
When chaining you need to ensure that any dependencies are copied across to the new copy that get's created(each chained method call results in a new JpaActiveSet being created). Here's an example, note how authors are copied across. 

    public Articles extends JpaActiveSet<Article> {
        
        private Authors authors;
        
        public Articles() {}
        public Articles(EntityManagerFactory emf, Authors authors) {
          super(Article.class, emf);
          this.authors = authors;
        }
      
        @Override
        protected <E extends JpaActiveSet<T>> void afterCopy(E copy) {
          copy.authors = authors;
        }
        
    }


  
### TODO - pretty self explanatory though
    Articles distinctArticles = articles.distinct();
    Articles articlesInList = articles.in(asList(article1, article2))
    Articles all = articles.all();
    Articles empty = articles.none();  
