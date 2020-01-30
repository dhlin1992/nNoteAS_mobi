package ntx.note.data;

import android.text.format.Time;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import junit.framework.Assert;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import ntx.note.CallbackEvent;
import ntx.note.Global;
import ntx.note.UndoManager;
import ntx.note.bookshelf.DateNoteData;
import ntx.note.bookshelf.QuickDateNoteData;
import ntx.note.bookshelf.QuickDateNoteDataMap;
import ntx.note.bookshelf.RecentlyNoteData;

import static ntx.note.Global.APP_DATA_PACKAGE_FILES_PATH;
import static ntx.note.Global.NOTEBOOK_DIRECTORY_PREFIX;

/**
 * The Bookshelf is a singleton holding the current Book
 * (fully loaded data) and light-weight Book for all books.
 */
public class Bookshelf {
    private static final String TAG = "Bookshelf";
    private static final String QUILL_EXTENSION = ".quill";

    //types for preview sorting
    public @interface PreviewOrder {
        int LAST_MODIFIED = 0;
        int NAME = 1;
        int CREATED_TIME = 2;
    }

    private static Bookshelf instance;

    private LinkedList<Book> data = new LinkedList<>();
    private LinkedList<Book> filterList = new LinkedList<>();
    private Book mCurrentBook;
    private Storage mStorage;
    private int mPreviewOrder = PreviewOrder.LAST_MODIFIED;
    private boolean mDisableBackup = false; // don't backup when download file from dropbox.

    public final static Book NullBook = new Book(Book.NULL_BOOK, true);

    /**
     * Constructor
     */
    private Bookshelf() {
        this.mStorage = Storage.getInstance();
        LinkedList<UUID> bookUUIDs = mStorage.listBookUUIDs();
        for (UUID uuid : bookUUIDs) {
            data.add(new Book(uuid, false));
        }

        if (!data.isEmpty()) {
            UUID currentBookUuid = mStorage.loadCurrentBookUUID();
            if (currentBookUuid == null)
                currentBookUuid = data.getFirst().getUUID();

            if (Global.getCurrentBook() != Bookshelf.NullBook) {
                currentBookUuid = Global.getCurrentBook().getUUID();
                mStorage.saveCurrentBookUUID(currentBookUuid);
            }

            mCurrentBook = new Book(currentBookUuid, true);
        } else {
            mCurrentBook = NullBook;
        }

    }

    /**
     * This is called automatically from the Storage initializer
     * <p>
     * 2019.06.26 Karote modifies.
     * It is modified as public.
     * It will not called automatically from the Storage initializer.
     * It should be called by programmer designed flow.
     */
    synchronized public static boolean initialize() {
        if (instance == null) {
            Log.v(TAG, "Reading notebook list from storage.");
            instance = new Bookshelf();
            /**
             * To do once sort to generate/update the JSON files.
             */
            instance.sortBookList();

            return false;
        } else {
            return true;
        }
    }

    /**
     * Getter
     */
    public static Bookshelf getInstance() {
        return instance;
    }

    public LinkedList<Book> getBookList() {
        LinkedList<Book> dataClone = new LinkedList<>();
        dataClone.addAll(data);
        return dataClone;
    }

    public LinkedList<Book> getFilterBookList(String keyword) {
        filterList.clear();
        for (Book book : data) {
            String bookTitleLowerCase = book.getTitle().toLowerCase();
            String keywordLowerCase = keyword.toLowerCase();
            if (bookTitleLowerCase.contains(keywordLowerCase))
                filterList.add(book);
        }
        return filterList;
    }

    public int getCount() {
        return data.size();
    }

    public Book getTempCurrentBook() {
        return mCurrentBook;
    }

    public int getPreviewOrder() {
        return mPreviewOrder;
    }

    /**
     * Setter
     */
    public void setCurrentBook(UUID uuid, boolean forceReload) {

        if (Global.getCurrentBook() != null
                && Global.getCurrentBook() != NullBook
                && Global.getCurrentBook().getUUID().equals(uuid)
                && mCurrentBook != null
                && mCurrentBook != NullBook
                && mCurrentBook.getUUID().equals(uuid)
                && !forceReload) {
            return;
        }

        boolean isUuidInList = false;
        for (Book datum : data) {
            if (datum.uuid.equals(uuid)) {
                isUuidInList = true;
                break;
            }
        }
        if (!isUuidInList)
            return;

        Book book = new Book(uuid, true);
        mCurrentBook = book;
        Global.setCurrentBook(book);
        UndoManager.getUndoManager().clearHistory();
        Global.getCurrentBook().setOnBookModifiedListener(UndoManager.getUndoManager());
        mStorage.saveCurrentBookUUID(uuid);
    }

    public boolean checkBookExist(UUID uuid) {
        boolean isUuidInList = false;
        for (Book datum : data) {
            if (datum.uuid.equals(uuid)) {
                isUuidInList = true;
                break;
            }
        }
        return isUuidInList;
    }

    public void setPreviewOrder(int previewOrder) {
        mPreviewOrder = previewOrder;
    }

    /**
     * Return the preview associated with the given UUID
     *
     * @param uuid
     * @return The Book with matching UUID or null.
     */
    public Book getBook(UUID uuid) {
        for (Book nb : data) {
            if (nb.getUUID().equals(uuid))
                return nb;
        }
        return null;
    }

    public void deleteBook(UUID uuid) {
        if (Global.getCurrentBook() != NullBook && uuid.equals(Global.getCurrentBook().uuid)) {
            Global.setCurrentBook(NullBook);
        }
        Book nb = getBook(uuid);
        if (nb == null)
            return;

        mStorage.getBookDirectory(uuid).deleteAll();
        data.remove(nb);

        sortBookList();
    }


    public void deleteBookNoSort(UUID uuid) {
        if (Global.getCurrentBook() != NullBook && uuid.equals(Global.getCurrentBook().uuid)) {
            Global.setCurrentBook(NullBook);
        }
        Book nb = getBook(uuid);
        if (nb == null)
            return;

        mStorage.getBookDirectory(uuid).deleteAll();
        data.remove(nb);
    }

    public void deleteStorageNotExistBook(UUID uuid) {
        if (Global.getCurrentBook() != NullBook && uuid.equals(Global.getCurrentBook().uuid)) {
            Global.setCurrentBook(NullBook);
        }
        Book nb = getBook(uuid);
        if (nb == null)
            return;
        data.remove(nb);

        sortBookList();
    }

    public void newBook(String title, boolean isLandscape) {
        Global.setCurrentBook(new Book(title, isLandscape));
        Global.getCurrentBook().save();
        sortBookList();
    }

    public void addBookToList(Book book) {
        if (data.contains(book))
            return;

        if (book != NullBook)
            data.add(book);
    }

    public void sortBookList() {
        Assert.assertNotNull(data);

        Collections.sort(data, new BookPreviewComparator());

        if ((mPreviewOrder == PreviewOrder.LAST_MODIFIED) || (mPreviewOrder == PreviewOrder.CREATED_TIME)) {
            Collections.reverse(data);
        }
        saveRecentJson();
        saveDateJsonFile();
        if (Global.getCurrentBook().isAllPageReady())
            saveQuickOpenNoteJsonFile();
        CallbackEvent callbackEvent = new CallbackEvent();
        callbackEvent.setMessage(CallbackEvent.SAVE_RECENTLY_NOTE_JSON_DONE);
        EventBus.getDefault().post(callbackEvent);
    }

    public void updateBookInList(UUID bookUuid) {
        data.remove(getBook(bookUuid));
        data.add(new Book(bookUuid, false));
    }

    /**
     * Private methods
     */
    private void saveRecentJson() {
        // ===== get note list 1~3 =====
        List<RecentlyNoteData> mRecentlyNoteList = new ArrayList<RecentlyNoteData>();
        RecentlyNoteData temp;

        for (int i = 0; i < (data.size() >= 3 ? 3 : data.size()); i++) {
            temp = new RecentlyNoteData(i);
            temp.setId(data.get(i).getUUID());
            temp.setLandscape(data.get(i).isLandscape);
            temp.setTitle(data.get(i).getTitle());
            mRecentlyNoteList.add(temp);
        }

        // ===== get recentNote.json list 1~3 compare with note list 1~3 =====
        Gson gson = new Gson();
        try {
            File folder = new File(APP_DATA_PACKAGE_FILES_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ======= Save =======
        Type listType = new TypeToken<List<RecentlyNoteData>>() {
        }.getType();
        String jsonStr = gson.toJson(mRecentlyNoteList, listType);

        FileWriter jsonFileWriter = null;
        try {
            String jsonFilePath = APP_DATA_PACKAGE_FILES_PATH + "recentNote.json";
            File file = new File(jsonFilePath);
            file.getParentFile().mkdirs();
            jsonFileWriter = new FileWriter(file);
            jsonFileWriter.write(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jsonFileWriter != null) {
                try {
                    jsonFileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class BookPreviewComparator implements Comparator<Book> {
        @Override
        public int compare(Book lhs, Book rhs) {
            if (mPreviewOrder == PreviewOrder.NAME) {
                return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
            } else if (mPreviewOrder == PreviewOrder.CREATED_TIME) {
                return Time.compare(lhs.getCtime(), rhs.getCtime());
            } else {// PreviewOrder.LAST_MODIFIED
                return Time.compare(lhs.getMtime(), rhs.getMtime());
            }
        }
    }

    public void saveDateJsonFile() {

        File oldFile = new File(Global.OLD_DATE_JSON_PATH);

        if (oldFile.exists()) {
            oldFile.delete();
        }

        Gson gson = new Gson();

        File folder = new File(Global.NEW_DATE_JSON_FOLDER);

        String jsonFilePath = Global.NEW_DATE_JSON_PATH;

        if (!folder.exists()) {
            folder.mkdirs();
        }

        List<DateNoteData> DateNoteDataList = new ArrayList<>();

        HashMap<String, ArrayList<QuickDateNoteData>> tempMap = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        for (Book book : Bookshelf.getInstance().getBookList()) {
            DateNoteData tempData = new DateNoteData();
            tempData.setTitle(book.getTitle());
            tempData.setId(book.getUUID());
            Time cTime = book.getCtime();
            tempData.setCreateTime(cTime.year + "/" + (cTime.month + 1) + "/" + cTime.monthDay + " " + cTime.hour + ":" + ((cTime.minute + "").equals("0") ? "00" : (cTime.minute + "")));
            Time mTime = book.getMtime();
            tempData.setModifyTime(mTime.year + "/" + (mTime.month + 1) + "/" + mTime.monthDay + " " + mTime.hour + ":" + ((mTime.minute + "").equals("0") ? "00" : (mTime.minute + "")));
            tempData.setCTime(book.getCtime());
            tempData.setMTime(book.getMtime());
            tempData.setLandscape(book.isLandscape);
            tempData.setVersion(book.getVersion());
            tempData.setPageNumber(book.pagesSize());
            tempData.setCurrentPage(book.currentPageNumber());
            DateNoteDataList.add(tempData);
            String title = book.getTitle();
            if (title.startsWith("Plan") && title.length() >= 12) {
                try {
                    //Try parse, check is correct format
                    String dayCode = title.substring(4, 12);
                    sdf.parse(dayCode);
                    QuickDateNoteData quickDateNoteData = new QuickDateNoteData();
                    quickDateNoteData.setId(book.getUUID());
                    quickDateNoteData.setTitle(book.title);
                    if (!tempMap.containsKey(dayCode)) {
                        ArrayList<QuickDateNoteData> tempArray = new ArrayList<>();
                        tempArray.add(quickDateNoteData);
                        tempMap.put(title.substring(4, 12), tempArray);
                    } else {
                        tempMap.get(dayCode).add(quickDateNoteData);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Type listType = new TypeToken<List<DateNoteData>>() {
        }.getType();

        String jsonStr = gson.toJson(DateNoteDataList, listType);

        FileWriter jsonFileWriter = null;
        try {
            File file = new File(jsonFilePath);
            file.getParentFile().mkdirs();
            jsonFileWriter = new FileWriter(file);
            jsonFileWriter.write(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jsonFileWriter != null) {
                try {
                    jsonFileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Type mapType = new TypeToken<QuickDateNoteDataMap>() {
        }.getType();

        QuickDateNoteDataMap quickDateNoteDataMap = new QuickDateNoteDataMap();

        quickDateNoteDataMap.setDateNoteDataMap(tempMap);

        String jsonStr2 = gson.toJson(quickDateNoteDataMap, mapType);

        FileWriter jsonFileWriter2 = null;
        try {
            File file = new File(Global.QUICK_DATE_JSON_PATH);
            file.getParentFile().mkdirs();
            jsonFileWriter2 = new FileWriter(file);
            jsonFileWriter2.write(jsonStr2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jsonFileWriter2 != null) {
                try {
                    jsonFileWriter2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveQuickOpenNoteJsonFile() {

        String folderPath = APP_DATA_PACKAGE_FILES_PATH + NOTEBOOK_DIRECTORY_PREFIX + Global.getCurrentBook().getUUID().toString() + "/";
        String jsonFilePath = folderPath + "quickOpen.json";

        Gson gson = new Gson();
        File folder = new File(folderPath);

        if (!folder.exists()) {
            return;
        }
        Book book = Global.getCurrentBook();
        DateNoteData tempData = new DateNoteData();
        tempData.setTitle(book.getTitle());
        tempData.setId(book.getUUID());
        Time cTime = book.getCtime();
        tempData.setCreateTime(cTime.year + "/" + (cTime.month + 1) + "/" + cTime.monthDay + " " + cTime.hour + ":" + ((cTime.minute + "").equals("0") ? "00" : (cTime.minute + "")));
        Time mTime = book.getMtime();
        tempData.setModifyTime(mTime.year + "/" + (mTime.month + 1) + "/" + mTime.monthDay + " " + mTime.hour + ":" + ((mTime.minute + "").equals("0") ? "00" : (mTime.minute + "")));
        tempData.setCTime(book.getCtime());
        tempData.setMTime(book.getMtime());
        tempData.setLandscape(book.isLandscape);
        tempData.setVersion(book.getVersion());
        tempData.setPageNumber(book.pagesSize());
        tempData.setCurrentPage(book.currentPageNumber());
        tempData.setCurrentPageUuid(book.currentPage().getUUID());

        String jsonStr = gson.toJson(tempData, new TypeToken<DateNoteData>() {
        }.getType());

        FileWriter jsonFileWriter = null;
        try {
            File file = new File(jsonFilePath);
            jsonFileWriter = new FileWriter(file);
            jsonFileWriter.write(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jsonFileWriter != null) {
                try {
                    jsonFileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
