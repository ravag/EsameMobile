package com.example.esamemobile.data.firebase.firestore

import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.example.esamemobile.data.Member
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import kotlin.String

interface GroupRepository {
    suspend fun addNewGroup(group: Group): Result<Unit>
    suspend fun readGroup(groupId: String): Result<Group?>
    suspend fun getAllUsersGroups(userId: String): Result<List<Group>>
    suspend fun deleteGroup(groupId: String): Result<Unit>
    suspend fun insertNewGroupMember(member: Member, groupId: String): Result<Unit>
    suspend fun removeGroupMember(userId: String, groupId: String): Result<Unit>
    suspend fun getAllGroupMembers(groupId: String): Result<List<Member>>
    suspend fun insertMemberCharacter(groupId: String, userId: String, character: Character): Result<Unit>
}

class GroupRepositoryImpl(private val db: FirebaseFirestore): GroupRepository {

    override suspend fun addNewGroup(group: Group): Result<Unit> {
      return try {
          val groupRef = db.collection("groups").document(group.id)
          val masterRef = groupRef.collection("members").document(group.masterId)

          val masterData = mapOf(
              "userId" to group.masterId,
              "userName" to group.masterName,
              "userImgUrl" to group.masterImgUrl,
              "groupName" to group.name,
              "groupImgUrl" to group.imageUrl
          )
          val groupData = mapOf(
              "name" to group.name,
              "imageUrl" to group.imageUrl,
              "description" to "",
              "nextSession" to ""
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
            Result.success(docs.map {
                Group(
                    id= it.reference.parent.parent!!.id,
                    name = it.getString("groupName") ?: "anonimo",
                    imageUrl = it.getString("groupImgUrl")
                )
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            db.collection("groups").document(groupId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertNewGroupMember(member: Member, groupId: String): Result<Unit> {
        return try {
            db.collection("groups").document(groupId)
                .collection("members").document(member.userId)
                .set(member).await()
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

    override suspend fun getAllGroupMembers(groupId: String): Result<List<Member>> {
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

}