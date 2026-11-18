package tech.huangsh.firstsight.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tech.huangsh.firstsight.data.model.Contact
import tech.huangsh.firstsight.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    // 联系人列表
    val contacts = contactRepository.allContacts

    // 对话框状态
    var showAddContactDialog by mutableStateOf(false)

    var showEditContactDialog by mutableStateOf(false)

    var showDeleteDialog by mutableStateOf(false)

    var editingContact by mutableStateOf<Contact?>(null)

    var deletingContact by mutableStateOf<Contact?>(null)

    // 系统联系人列表
    private val _systemContacts = MutableStateFlow<List<Contact>>(emptyList())
    val systemContacts = _systemContacts.asStateFlow()

    private val _isLoadingSystemContacts = MutableStateFlow(false)
    val isLoadingSystemContacts = _isLoadingSystemContacts.asStateFlow()

    /**
     * 添加联系人
     */
    fun addContact(contact: Contact) {
        viewModelScope.launch {
            android.util.Log.d("ContactViewModel", "Adding contact: ${contact.name}, avatarUri: ${contact.avatarUri}")
            contactRepository.insertContact(contact)
        }
    }

    /**
     * 更新联系人
     */
    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            android.util.Log.d("ContactViewModel", "Updating contact: ${contact.name}, avatarUri: ${contact.avatarUri}")
            contactRepository.updateContact(contact)
        }
    }

    /**
     * 删除联系人
     */
    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
        }
    }

    /**
     * 编辑联系人
     */
    fun editContact(contact: Contact) {
        editingContact = contact
        showEditContactDialog = true
    }

    /**
     * 显示删除确认
     */
    fun showDeleteConfirmation(contact: Contact) {
        deletingContact = contact
        showDeleteDialog = true
    }

    /**
     * 移动联系人位置
     */
    fun moveContact(contactId: Long, direction: Int) {
        viewModelScope.launch {
            val contacts = contactRepository.allContacts.first()
            val currentIndex = contacts.indexOfFirst { it.id == contactId }
            if (currentIndex != -1) {
                val newIndex = currentIndex + direction
                if (newIndex >= 0 && newIndex < contacts.size) {
                    contactRepository.moveContact(contactId, currentIndex, newIndex)
                }
            }
        }
    }

    /**
     * 批量保存联系人排序
     */
    fun saveContactOrder(orderedContacts: List<Contact>) {
        viewModelScope.launch {
            contactRepository.updateContactOrders(orderedContacts)
        }
    }

    /**
     * 加载系统联系人
     */
    fun loadSystemContacts() {
        viewModelScope.launch {
            _isLoadingSystemContacts.value = true
            _systemContacts.value = contactRepository.getSystemContacts()
            _isLoadingSystemContacts.value = false
        }
    }

    /**
     * 批量导入系统通讯录联系人
     */
    fun importContacts(selectedContacts: List<Contact>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = contactRepository.importContacts(selectedContacts)
            onComplete(count)
        }
    }
}