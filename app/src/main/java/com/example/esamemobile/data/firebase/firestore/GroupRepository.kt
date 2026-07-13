package com.example.esamemobile.data.firebase.firestore

import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.example.esamemobile.data.Member
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import kotlin.String

interface GroupRepository {
    suspend fun addNewGroup(group: Group, username: String, imgUrl: String): Result<Unit>
    suspend fun readGroup(groupId: String): Result<Group?>
    suspend fun getAllUsersGroups(userId: String): Result<List<Group>>
    suspend fun deleteGroup(groupId: String): Result<Unit>
    suspend fun insertNewGroupMember(member: Member, inviteCode: String): Result<Unit>
    suspend fun removeGroupMember(userId: String, groupId: String): Result<Unit>
    suspend fun getAllGroupMembers(groupId: String?): Result<List<Member>>
    suspend fun insertMemberCharacter(groupId: String, userId: String, character: Character): Result<Unit>
    suspend fun updateGroup(group: Group): Result<Unit>
    suspend fun insertSessionDate(groupId: String, date: Timestamp): Result<Unit>
    suspend fun updateGroupImage(groupId: String, url: String): Result<Unit>
}

class GroupRepositoryImpl(private val db: FirebaseFirestore): GroupRepository {

    private val codeChars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

    private fun generateInviteCode(length: Int = 8): String =
        (1..length).map { codeChars.random() }.joinToString("")

    private suspend fun generateUniqueInviteCode(): String {
        while (true) {
            val code = generateInviteCode()
            val existing = db.collection("groups")
                .whereEqualTo("inviteCode", code)
                .limit(1)
                .get().await()
            if (existing.isEmpty) return code
        }
    }

    override suspend fun addNewGroup(group: Group, username: String, imgUrl: String): Result<Unit> {
      return try {
          val groupRef = db.collection("groups").document(group.id)
          val masterRef = groupRef.collection("members").document(group.masterId)

          val masterData = mapOf(
              "userId" to group.masterId,
              "username" to username,
              "userImgUrl" to imgUrl
          )
          val groupData = mapOf(
              "name" to group.name,
              "imageUrl" to group.imageUrl,
              "description" to "",
              "nextSession" to null,
              "masterId" to group.masterId,
              "inviteCode" to generateUniqueInviteCode(),
              "lastNotifiedSession" to null,
              "pendingDeletion" to false
          )

          db.runBatch { batch ->
              batch.set(groupRef,groupData)
              batch.set(masterRef,masterData)
          }.await()

          Result.success(Unit)
      } catch (e: Exception) {
          Result.failure(e)
      }
    }

    override suspend fun readGroup(groupId: String): Result<Group?> {
        return try {
            val doc = db.collection("groups")
                .document(groupId).get()
                .await()
            Result.success(doc.toObject<Group>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsersGroups(userId: String): Result<List<Group>> {
        return try {
            val docs = db.collectionGroup("members")
                .whereEqualTo("userId",userId)
                .get().await()

            val groupRefs = docs.mapNotNull { it.reference.parent.parent }
            if (groupRefs.isEmpty()) {
                Result.success(emptyList())
            } else {
                val groups = groupRefs.chunked(30).flatMap { chunk ->
                    db.collection("groups")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get().await()
                        .toObjects(Group::class.java)
                }
                Result.success(groups)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            db.collection("groups").document(groupId)
                .update("pendingDeletion",true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertNewGroupMember(member: Member, inviteCode: String): Result<Unit> {
        return try {
            val doc = db.collection("groups")
                .whereEqualTo("inviteCode",inviteCode.uppercase())
                .limit(1)
                .get().await()

            val groupDoc = doc.documents.firstOrNull() ?: return Result.failure(Exception("Gruppo inesistente"))
            val groupRef = groupDoc.reference
            val memberRef = groupRef.collection("members").document(member.userId)
            val alreadyExists = memberRef.get().await()
            if (alreadyExists.exists()) return Result.failure(Exception("Membro già esistente"))

            db.runBatch { batch ->
                batch.set(memberRef, member)
                batch.update(groupRef,"partecipants", FieldValue.arrayUnion(member.userId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeGroupMember(userId: String, groupId: String): Result<Unit> {
        return try {
            db.collection("groups").document(groupId)
                .collection("members").document(userId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllGroupMembers(groupId: String?): Result<List<Member>> {
        if (groupId == null) return Result.failure(IllegalStateException("Nessun id passato"))
        return try {
            val docs = db.collection("groups").document(groupId)
                .collection("members").get()
                .await()
            Result.success(docs.map { it.toObject<Member>() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertMemberCharacter(
        groupId: String,
        userId: String,
        character: Character
    ): Result<Unit> {
        return try {
            db.collection("groups").document(groupId)
                .collection("members").document(userId)
                .update( mapOf(
                    "characterId" to character.id,
                    "characterName" to character.name,
                    "characterImgUrl" to character.imageUrl
                ))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGroup(group: Group): Result<Unit> {
        return try {
            db.collection("groups").document(group.id)
                .update(mapOf(
                    "name" to group.name,
                    "description" to group.description,
                    "imageUrl" to group.imageUrl
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertSessionDate(groupId: String, date: Timestamp): Result<Unit> {
        return try {
            db.collection("groups").document(groupId)
                .update(mapOf("nextSession" to date)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGroupImage(groupId: String, url: String): Result<Unit> {
        return try {
            db.collection("groups").document(groupId)
                .update(mapOf("imageUrl" to url)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}