package com.dianping.dobby.book.biz;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.dobby.DobbyConstants;
import com.dianping.dobby.book.model.entity.Book;
import com.dianping.dobby.book.model.entity.BookModel;
import com.dianping.dobby.book.model.transform.DefaultSaxParser;
import com.dianping.dobby.email.MessageHandler;
import com.dianping.dobby.email.MessagePayload;

public class BookCommandTest extends ComponentTestCase {
   private static StringBuilder s_sb = new StringBuilder();

   private void check(String cmd, String by, String expected, String... params) throws Exception {
      MessageHandler handler = lookup(MessageHandler.class, DobbyConstants.ID_BOOK);
      MessagePayload payload = new MessagePayload();

      payload.setCommand(cmd);
      payload.setCommandParams(params);
      payload.setFrom(by);

      s_sb.setLength(0);
      handler.handle(payload);
      Assert.assertEquals(expected, s_sb.toString());
   }

   @Test
   public void testBorrowAndReturn() throws Exception {
      check("borrow", "yong.you@dianping.com", "onBookBorrowSuccessful", "101");
      check("borrow", "yong.you@dianping.com", "onBookAlreadyBorrowed", "101");
      check("return", "yong.you@dianping.com", "onBookReturnSuccessful", "101");
      check("return", "yong.you@dianping.com", "onReturnBookNotBorrowed", "101");

      check("borrow", "yong.you@dianping.com", "onBookNotFound", "101***");
      check("borrow", "qimin.wu@dianping.com", "onBookBorrowSuccessful", "101");
      check("borrow", "yix.zhang@dianping.com", "onBookBorrowSuccessful", "101");
      check("borrow", "hao.zhu@dianping.com", "onNoBookToBorrow", "101");
      check("return", "yix.zhang@dianping.com", "onBookReturnSuccessful", "101");
      check("borrow", "hao.zhu@dianping.com", "onBookBorrowSuccessful", "101");
   }

   @Test
   public void testHelp() throws Exception {
      check("help", "qimin.wu@dianping.com", "onShowAllAvailableBookList");
   }

   public static class MockBookManager implements BookManager, Initializable {
      private BookModel m_model;

      @Override
      public String buildCsv(boolean availableOnly) {
         return null;
      }

      @Override
      public List<Book> findAllBooks(boolean activeOnly) {
         return Collections.emptyList();
      }

      @Override
      public List<Book> findAllBorrowedBooksBy(String borrower) {
         return Collections.emptyList();
      }

      @Override
      public Book findBookById(String id) {
         return m_model.findBook(id);
      }

      @Override
      public BookModel getModel() {
         return m_model;
      }

      @Override
      public void initialize() throws InitializationException {
         try {
            InputStream in = getClass().getResourceAsStream("book.xml");
            String xml = Files.forIO().readFrom(in, "utf-8");

            m_model = DefaultSaxParser.parse(xml);
         } catch (Exception e) {
            throw new InitializationException(String.format("Unable to load books from resource(book.xml)!"), e);
         }
      }

      @Override
      public void save() {
      }
   }

   public static class MockBookMessageHandler extends BookMessageHandler {
      @Override
      public void onBookAlreadyBorrowed(MessagePayload payload, Book book) {
         s_sb.append("onBookAlreadyBorrowed");
      }

      @Override
      public void onBookBorrowSuccessful(MessagePayload payload, Book book) {
         s_sb.append("onBookBorrowSuccessful");
      }

      @Override
      public void onBookNotFound(MessagePayload payload, String bookId) {
         s_sb.append("onBookNotFound");
      }

      @Override
      public void onBookReturnSuccessful(MessagePayload payload, Book book) {
         s_sb.append("onBookReturnSuccessful");
      }

      @Override
      public void onNoBookToBorrow(MessagePayload payload, Book book) {
         s_sb.append("onNoBookToBorrow");
      }

      @Override
      public void onNoBookToReturn(MessagePayload payload, Book book) {
         s_sb.append("onNoBookToReturn");
      }

      @Override
      public void onReturnBookNotBorrowed(MessagePayload payload, Book book) {
         s_sb.append("onReturnBookNotBorrowed");
      }

      @Override
      public void onShowAllAvailableBookList(MessagePayload payload, List<Book> all, List<Book> borrowed) {
         s_sb.append("onShowAllAvailableBookList");
      }
   }
}
